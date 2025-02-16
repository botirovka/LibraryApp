package com.botirovka.libraryapp.reviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.example.domain.model.Review

class ReviewAdapter: RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var reviews: List<Review> = emptyList()

    fun submitList(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewAdapter.ReviewViewHolder, position: Int) {
        val currentReview = reviews[position]
        holder.bind(currentReview)
    }

    override fun getItemCount() = reviews.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)

        fun bind(review: Review){
            usernameTextView.text = review.username
            ratingBar.rating = review.rating
            reviewTextView.text = review.reviewText
        }
    }
}