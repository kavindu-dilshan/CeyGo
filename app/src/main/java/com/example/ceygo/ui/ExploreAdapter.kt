package com.example.ceygo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.ceygo.R
import com.example.ceygo.model.Location

class ExploreAdapter(
    private val onClick: (Location) -> Unit
) : RecyclerView.Adapter<ExploreAdapter.VH>() {

    private val items = mutableListOf<Location>()

    fun submit(list: List<Location>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgCover)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
        val rating: TextView = v.findViewById(R.id.tvRating)
        val fav: ImageView = v.findViewById(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_explore, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val loc = items[position]

        holder.title.text = loc.name
        holder.subtitle.text = "${loc.district}, ${loc.province}"
        holder.rating.text = String.format("%.1f", loc.ratingAverage)

        // Image can be a URL or a drawable name in loc.images[0]
        val first = loc.images.firstOrNull()
        if (first.isNullOrBlank()) {
            holder.img.setImageResource(R.drawable.ic_avatar_placeholder) // optional fallback
        } else {
            // Try resolve drawable name; if not found, Coil will load as URL
            val resId = holder.img.resources.getIdentifier(
                first, "drawable", holder.img.context.packageName
            )
            if (resId != 0) holder.img.setImageResource(resId) else holder.img.load(first)
        }

        holder.v.setOnClickListener { onClick(loc) }
        // holder.fav.setOnClickListener { /* toggle favorite if you want */ }
    }
}