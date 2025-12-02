package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.databinding.ItemReviewBinding
import com.carpooling.app.models.Review
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private var reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            // Display reviewer name or reviewer ID if name is not available
            binding.tvReviewerName.text = review.reviewerName.ifEmpty {
                "User: ${review.reviewerId.take(8)}"
            }

            // Display rating
            binding.ratingBar.rating = review.rating.toFloat()
            binding.tvRating.text = review.rating.toString()

            // Display comment
            binding.tvComment.text = review.comment.ifEmpty { "No comment provided" }

            // Display date
            binding.tvReviewDate.text = formatDate(review.createdAt)

            // Display ride reference
            binding.tvRideReference.text = "Ride: #${review.rideId.take(8)}"
        }

        private fun formatDate(dateString: String): String {
            return if (dateString.isEmpty()) {
                "Recently"
            } else {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = sdf.parse(dateString)
                    val now = Calendar.getInstance().time
                    val diffMillis = now.time - (date?.time ?: 0)
                    val diffDays = diffMillis / (24 * 60 * 60 * 1000)

                    when {
                        diffDays == 0L -> "Today"
                        diffDays == 1L -> "Yesterday"
                        diffDays < 7 -> "$diffDays days ago"
                        diffDays < 30 -> "${diffDays / 7} weeks ago"
                        else -> "${diffDays / 30} months ago"
                    }
                } catch (e: Exception) {
                    "Recently"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}

