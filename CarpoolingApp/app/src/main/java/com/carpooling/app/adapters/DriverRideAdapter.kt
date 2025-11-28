package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.databinding.ItemDriverRideBinding
import com.carpooling.app.models.Ride
import java.text.NumberFormat
import java.util.Locale

class DriverRideAdapter(
    private var rides: List<Ride>,
    private val onDeleteClick: (Ride) -> Unit
) : RecyclerView.Adapter<DriverRideAdapter.DriverRideViewHolder>() {
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    inner class DriverRideViewHolder(private val binding: ItemDriverRideBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(ride: Ride) {
            binding.tvRoute.text = "${ride.from} → ${ride.to}"
            binding.tvDate.text = "Date: ${ride.date}"
            binding.tvSeats.text = "Available seats: ${ride.availableSeats}"
            binding.tvPrice.text = priceFormat.format(ride.price)
            binding.tvStatus.text = "Status: ${ride.status}"
            
            // Set status color
            val statusColor = when (ride.status) {
                "SCHEDULED" -> android.graphics.Color.parseColor("#28a745")
                "IN_PROGRESS" -> android.graphics.Color.parseColor("#ffc107")
                "COMPLETED" -> android.graphics.Color.parseColor("#6c757d")
                "CANCELED" -> android.graphics.Color.parseColor("#dc3545")
                else -> android.graphics.Color.parseColor("#6c757d")
            }
            binding.tvStatus.setTextColor(statusColor)
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(ride)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverRideViewHolder {
        val binding = ItemDriverRideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DriverRideViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: DriverRideViewHolder, position: Int) {
        holder.bind(rides[position])
    }
    
    override fun getItemCount() = rides.size
    
    fun updateRides(newRides: List<Ride>) {
        rides = newRides
        notifyDataSetChanged()
    }
}
