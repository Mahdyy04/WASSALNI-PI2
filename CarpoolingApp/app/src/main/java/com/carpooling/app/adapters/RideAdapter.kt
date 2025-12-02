package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.databinding.ItemRideBinding
import com.carpooling.app.models.Ride
import java.text.NumberFormat
import java.util.Locale

class RideAdapter(
    private var rides: List<Ride>,
    private val onBookClick: (Ride) -> Unit
) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    inner class RideViewHolder(private val binding: ItemRideBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(ride: Ride) {
            binding.tvRoute.text = "${ride.from} → ${ride.to}"
            binding.tvDriver.text = "Driver ID: ${ride.driverId.take(8)}..."
            binding.tvDate.text = "Date: ${ride.date}"
            binding.tvSeats.text = "Seats: ${ride.availableSeats} available"
            binding.tvPrice.text = "${priceFormat.format(ride.price)}/seat"
            
            // Disable booking if no seats available
            binding.btnBook.isEnabled = ride.availableSeats > 0
            binding.btnBook.alpha = if (ride.availableSeats > 0) 1.0f else 0.5f
            
            binding.btnBook.setOnClickListener {
                onBookClick(ride)
            }
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
