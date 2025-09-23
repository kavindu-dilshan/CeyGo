package com.example.ceygo.ui.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ceygo.R
import com.example.ceygo.model.Review

class ReviewAdapter :
    ListAdapter<Review, ReviewAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(old: Review, new: Review) = old.id == new.id
        override fun areContentsTheSame(old: Review, new: Review) = old == new
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val rating: RatingBar = view.findViewById(R.id.ratingSmall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = getItem(pos)
        h.tvAuthor.text = it.author
        h.tvText.text = it.text
        h.rating.rating = it.rating.toFloat()
    }
}