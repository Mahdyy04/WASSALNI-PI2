package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.databinding.ItemBookingBinding
import com.carpooling.app.models.Booking
import java.text.NumberFormat
import java.util.Locale

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onCancelClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    inner class BookingViewHolder(private val binding: ItemBookingBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(booking: Booking) {
            // Display ride info if available
            val ride = booking.ride
            if (ride != null) {
                binding.tvRoute.text = "${ride.from} → ${ride.to}"
                binding.tvDate.text = "Date: ${ride.date}"
                binding.tvPrice.text = priceFormat.format(ride.price * booking.seatsBooked)
            } else {
                binding.tvRoute.text = "Ride #${booking.rideId.take(8)}"
                binding.tvDate.text = "Loading..."
                binding.tvPrice.visibility = View.GONE
            }
            
            binding.tvSeats.text = "Seats: ${booking.seatsBooked}"
            binding.tvStatus.text = "Status: ${booking.status}"
            
            // Set status color
            val statusColor = when (booking.status) {
                "ACCEPTED" -> android.graphics.Color.parseColor("#28a745")
                "PENDING" -> android.graphics.Color.parseColor("#ffc107")
                "REJECTED", "CANCELLED" -> android.graphics.Color.parseColor("#dc3545")
                else -> android.graphics.Color.parseColor("#6c757d")
            }
            binding.tvStatus.setTextColor(statusColor)
            
            // Only show cancel button for pending bookings
            if (booking.status == "PENDING" || booking.status == "ACCEPTED") {
                binding.btnCancel.visibility = View.VISIBLE
                binding.btnCancel.setOnClickListener {
                    onCancelClick(booking)
                }
            } else {
                binding.btnCancel.visibility = View.GONE
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }
    
    override fun getItemCount() = bookings.size
    
    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
