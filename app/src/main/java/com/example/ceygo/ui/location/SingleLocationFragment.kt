package com.example.ceygo.ui.location

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.example.ceygo.R
import com.example.ceygo.model.Review
import com.example.ceygo.ui.LocationsViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class SingleLocationFragment : Fragment(R.layout.fragment_single_location) {

    private val vm: LocationsViewModel by activityViewModels()
    private val locationId: String by lazy { requireArguments().getString(ARG_ID)!! }

    private lateinit var reviewAdapter: ReviewAdapter
    private var expanded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reviews list
        reviewAdapter = ReviewAdapter()
        view.findViewById<RecyclerView>(R.id.rvReviews).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
            isNestedScrollingEnabled = false
        }

        // Read more toggle
        val tvDesc = view.findViewById<TextView>(R.id.tvDescription)
        val btnRead = view.findViewById<TextView>(R.id.btnReadMore)
        btnRead.setOnClickListener {
            expanded = !expanded
            tvDesc.maxLines = if (expanded) Int.MAX_VALUE else 3
            btnRead.text = if (expanded) "Read Less" else "Read More"
        }

        // Observe location
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)
        val tvProvince = view.findViewById<TextView>(R.id.tvProvince)
        val tvDistrict = view.findViewById<TextView>(R.id.tvDistrict)
        val pager = view.findViewById<ViewPager2>(R.id.pagerImages)

        pager.adapter = ImagePagerAdapter() // defined below

        lifecycleScope.launch {
            vm.location(locationId)
                .filterNotNull()
                .collect { loc ->
                    tvName.text = loc.name
                    tvSubtitle.text = "${loc.name}, Sri Lanka"
                    tvProvince.text = "Province: ${loc.province}"
                    tvDistrict.text = "District: ${loc.district}"
                    tvDesc.text = loc.description
                    (pager.adapter as ImagePagerAdapter).submit(loc.images)
                    reviewAdapter.submitList(loc.reviews.toList())
                }
        }

        // Open bottom sheet
        view.findViewById<View>(R.id.btnWriteReview).setOnClickListener {
            WriteReviewSheet.newInstance(locationId)
                .show(parentFragmentManager, "write_review")
        }

        // Receive result from bottom sheet
        parentFragmentManager.setFragmentResultListener(
            WriteReviewSheet.RESULT_KEY, viewLifecycleOwner
        ) { _, b ->
            val id = b.getString("locationId") ?: return@setFragmentResultListener
            val text = b.getString("text").orElse("")
            val rating = b.getInt("rating")
            if (text.isNotBlank()) {
                vm.addReview(id, Review(author = "You", rating = rating, text = text))
            }
        }
    }

    private fun String?.orElse(def: String) = this ?: def

    companion object {
        private const val ARG_ID = "id"
        fun newInstance(locationId: String) = SingleLocationFragment().apply {
            arguments = bundleOf(ARG_ID to locationId)
        }
    }
}

/** Simple images pager **/
private class ImagePagerAdapter :
    androidx.recyclerview.widget.RecyclerView.Adapter<ImageVH>() {

    private val items = mutableListOf<String>()

    fun submit(list: List<String>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, vType: Int): ImageVH {
        val iv = android.widget.ImageView(p.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
        return ImageVH(iv)
    }

    override fun onBindViewHolder(h: ImageVH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size
}

private class ImageVH(private val iv: android.widget.ImageView) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(iv) {
    fun bind(nameOrUrl: String) {
        // If it's a URL -> Coil will load it.
        // If it's a drawable name -> resolve to resId.
        val ctx = iv.context
        val resId = ctx.resources.getIdentifier(nameOrUrl, "drawable", ctx.packageName)
        if (resId != 0) iv.setImageResource(resId) else iv.load(nameOrUrl)
    }
}