package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.R
import com.carpooling.app.databinding.ItemRideBinding
import com.carpooling.app.models.Ride
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying all rides in admin rides history view.
 */
class AdminRideAdapter(
    private var rides: List<Ride>
) : RecyclerView.Adapter<AdminRideAdapter.RideViewHolder>() {
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    inner class RideViewHolder(private val binding: ItemRideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(ride: Ride) {
            binding.tvRoute.text = "${ride.from} → ${ride.to}"
            binding.tvDate.text = "Date: ${ride.date}"
            binding.tvSeats.text = "Available seats: ${ride.availableSeats}"
            binding.tvPrice.text = priceFormat.format(ride.price)
            
            // Show ride status
            binding.tvDriver.text = "Status: ${ride.status}"
            
            // Color code status
            val statusColor = when (ride.status) {
                "SCHEDULED" -> ContextCompat.getColor(itemView.context, R.color.primary)
                "IN_PROGRESS" -> ContextCompat.getColor(itemView.context, R.color.status_warning)
                "COMPLETED" -> ContextCompat.getColor(itemView.context, R.color.status_success)
                "CANCELLED" -> ContextCompat.getColor(itemView.context, R.color.status_danger)
                else -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            binding.tvDriver.setTextColor(statusColor)
            
            // Hide book button in admin view
            binding.btnBook.visibility = android.view.View.GONE
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val binding = ItemRideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RideViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(rides[position])
    }
    
    override fun getItemCount() = rides.size
    
    fun updateRides(newRides: List<Ride>) {
        rides = newRides
        notifyDataSetChanged()
    }
}
