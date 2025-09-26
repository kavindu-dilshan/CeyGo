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

class NearbyAdapter(
    private val onClick: (Location) -> Unit
) : RecyclerView.Adapter<NearbyAdapter.VH>() {

    private val items = mutableListOf<Location>()
    fun submit(list: List<Location>) { items.apply { clear(); addAll(list) }; notifyDataSetChanged() }

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgCover)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val rating: TextView = v.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_card_popular, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val loc = items[pos]
        h.title.text = loc.name
        h.rating.text = String.format("%.1f", loc.ratingAverage)

        val first = loc.images.firstOrNull()
        if (first.isNullOrBlank()) {
            h.img.setImageResource(R.drawable.ic_avatar_placeholder)
        } else {
            val resId = h.img.resources.getIdentifier(first, "drawable", h.img.context.packageName)
            if (resId != 0) h.img.setImageResource(resId) else h.img.load(first)
        }

        h.v.setOnClickListener { onClick(loc) }
    }
}