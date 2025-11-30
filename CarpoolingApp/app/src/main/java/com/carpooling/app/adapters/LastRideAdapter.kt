package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.R
import com.carpooling.app.databinding.ItemLastRideBinding
import com.carpooling.app.models.Booking
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying confirmed rides with review and report functionality.
 * 
 * Shows rides where booking status is ACCEPTED, with a review button
 * that is disabled once the user has already reviewed that ride,
 * and a report button to report the driver.
 */
class LastRideAdapter(
    private var bookings: List<Booking>,
    private var reviewedRideIds: Set<String>,
    private val onReviewClick: (Booking) -> Unit,
    private val onReportClick: (Booking) -> Unit
) : RecyclerView.Adapter<LastRideAdapter.LastRideViewHolder>() {
    
    companion object {
        /** Number of characters to display when showing truncated ride ID */
        private const val RIDE_ID_DISPLAY_LENGTH = 8
    }
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    inner class LastRideViewHolder(private val binding: ItemLastRideBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(booking: Booking) {
            // Display ride info if available
            val ride = booking.ride
            if (ride != null) {
                binding.tvRoute.text = "${ride.from} → ${ride.to}"
                binding.tvDate.text = "Date: ${ride.date}"
                // Show total price for the passenger's booking
                val totalPrice = ride.price * booking.seatsBooked
                if (booking.seatsBooked > 1) {
                    // Show breakdown when multiple seats are booked
                    binding.tvPrice.text = "Total: ${priceFormat.format(totalPrice)} (${booking.seatsBooked} × ${priceFormat.format(ride.price)})"
                } else {
                    binding.tvPrice.text = priceFormat.format(totalPrice)
                }
                binding.tvPrice.visibility = View.VISIBLE
            } else {
                binding.tvRoute.text = "Ride #${booking.rideId.take(RIDE_ID_DISPLAY_LENGTH)}"
                binding.tvDate.text = "Loading..."
                binding.tvPrice.visibility = View.GONE
            }
            
            binding.tvSeats.text = "Seats booked: ${booking.seatsBooked}"
            binding.tvStatus.text = "Status: Confirmed ✓"
            binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_success))
            
            // Check if this ride has already been reviewed
            val isReviewed = reviewedRideIds.contains(booking.rideId)
            
            if (isReviewed) {
                binding.btnReview.visibility = View.GONE
                binding.tvReviewed.visibility = View.VISIBLE
                binding.ratingBar.visibility = View.GONE
            } else {
                binding.btnReview.visibility = View.VISIBLE
                binding.tvReviewed.visibility = View.GONE
                binding.ratingBar.visibility = View.VISIBLE
                binding.ratingBar.rating = 0f
                
                binding.btnReview.setOnClickListener {
                    onReviewClick(booking)
                }
            }
            
            // Report button is always visible for confirmed rides
            binding.btnReport.setOnClickListener {
                onReportClick(booking)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastRideViewHolder {
        val binding = ItemLastRideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LastRideViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LastRideViewHolder, position: Int) {
        holder.bind(bookings[position])
    }
    
    override fun getItemCount() = bookings.size
    
    fun updateBookings(newBookings: List<Booking>, newReviewedRideIds: Set<String>) {
        bookings = newBookings
        reviewedRideIds = newReviewedRideIds
        notifyDataSetChanged()
    }
}
