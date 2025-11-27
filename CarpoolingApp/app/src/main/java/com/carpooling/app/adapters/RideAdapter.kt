package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.databinding.ItemRideBinding
import com.carpooling.app.models.Ride

class RideAdapter(
    private var rides: List<Ride>,
    private val onBookClick: (Ride) -> Unit
) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {
    
    inner class RideViewHolder(private val binding: ItemRideBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(ride: Ride) {
            binding.tvRoute.text = "${ride.from} → ${ride.to}"
            binding.tvDriver.text = "Driver: ${ride.driverName}"
            binding.tvDate.text = "Date: ${ride.date}"
            binding.tvSeats.text = "Seats: ${ride.availableSeats} available"
            binding.tvPrice.text = "$${ride.pricePerSeat}"
            
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
