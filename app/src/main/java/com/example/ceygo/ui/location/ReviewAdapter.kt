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
import android.widget.ImageView
import coil.load
import androidx.core.content.ContextCompat

class ReviewAdapter :
    ListAdapter<Review, ReviewAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(old: Review, new: Review) = old.id == new.id
        override fun areContentsTheSame(old: Review, new: Review) = old == new
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val rating: RatingBar = view.findViewById(R.id.ratingSmall)
    }

    var onLongPress: ((Review) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val review = getItem(pos)
        h.tvAuthor.text = review.author
        h.tvText.text = review.text
        h.rating.rating = review.rating.toFloat()
        // Load avatar (fallback icon is set in XML)
        review.userPhoto?.let { url -> if (url.isNotBlank()) h.imgAvatar.load(url) }
        // Highlight owned reviews by tinting author text
        val color = if (review.isOwner) R.color.primaryBlue else android.R.color.black
        h.tvAuthor.setTextColor(ContextCompat.getColor(h.itemView.context, color))
        // Long press to delete only if owner
        h.itemView.setOnLongClickListener {
            if (review.isOwner) {
                onLongPress?.invoke(review)
                true
            } else {
                false
            }
        }
    }
}