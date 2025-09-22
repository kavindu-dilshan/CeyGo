package com.example.ceygo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.ceygo.R
import com.example.ceygo.model.Destination
import java.util.Locale

class NearbyAdapter(
    private val onClick: (Destination) -> Unit
) : RecyclerView.Adapter<NearbyAdapter.VH>() {
    private val items = mutableListOf<Destination>()
    fun submit(list: List<Destination>) { items.apply { clear(); addAll(list) }; notifyDataSetChanged() }

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val img = v.findViewById<ImageView>(R.id.imgCover)
        val title = v.findViewById<TextView>(R.id.tvTitle)
        val rating = v.findViewById<TextView>(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_card_nearby, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = items[pos]
        h.title.text = d.title
        h.rating.text = String.format(Locale.US, "%.1f", d.rating)
        h.v.setOnClickListener { onClick(d) }
        h.img.load(d.imageUrl)
    }
}