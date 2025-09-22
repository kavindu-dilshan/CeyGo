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

class ExploreAdapter(
    private val onClick: (Destination) -> Unit
) : RecyclerView.Adapter<ExploreAdapter.VH>() {

    private val items = mutableListOf<Destination>()

    fun submit(list: List<Destination>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val img = v.findViewById<ImageView>(R.id.imgCover)
        val title = v.findViewById<TextView>(R.id.tvTitle)
        val subtitle = v.findViewById<TextView>(R.id.tvSubtitle)
        val rating = v.findViewById<TextView>(R.id.tvRating)
        val fav = v.findViewById<ImageView>(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_card_explore, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        holder.title.text = d.title
        holder.subtitle.text = d.subtitle
        holder.rating.text = String.format("%.1f", d.rating)
        holder.v.setOnClickListener { onClick(d) }
        holder.img.load(d.imageUrl)
    }
}
