package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.databinding.ItemPendingBookingBinding
import com.carpooling.app.models.Booking
import java.text.NumberFormat
import java.util.Locale

class PendingBookingAdapter(
    private var bookings: List<Booking>,
    private val onAcceptClick: (Booking) -> Unit,
    private val onRejectClick: (Booking) -> Unit,
    private val onViewProfileClick: (Booking) -> Unit
) : RecyclerView.Adapter<PendingBookingAdapter.PendingBookingViewHolder>() {
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    inner class PendingBookingViewHolder(private val binding: ItemPendingBookingBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(booking: Booking) {
            val ride = booking.ride
            if (ride != null) {
                binding.tvRoute.text = "${ride.from} → ${ride.to}"
                binding.tvDate.text = "Date: ${ride.date}"
                binding.tvPrice.text = priceFormat.format(ride.price * booking.seatsBooked)
            } else {
                binding.tvRoute.text = "Ride #${booking.rideId.take(8)}"
                binding.tvDate.text = "Loading..."
                binding.tvPrice.text = ""
            }
            
            binding.tvPassenger.text = "Passenger: ${booking.passengerId.take(8)}..."
            binding.tvSeats.text = "Seats requested: ${booking.seatsBooked}"
            
            binding.btnViewProfile.setOnClickListener {
                onViewProfileClick(booking)
            }

            binding.btnAccept.setOnClickListener {
                onAcceptClick(booking)
            }
            
            binding.btnReject.setOnClickListener {
                onRejectClick(booking)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingBookingViewHolder {
        val binding = ItemPendingBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PendingBookingViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PendingBookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }
    
    override fun getItemCount() = bookings.size
    
    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
