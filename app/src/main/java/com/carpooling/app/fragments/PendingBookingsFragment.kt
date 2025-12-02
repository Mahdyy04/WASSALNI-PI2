package com.carpooling.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.PassengerProfileActivity
import com.carpooling.app.adapters.PendingBookingAdapter
import com.carpooling.app.databinding.FragmentPendingBookingsBinding
import com.carpooling.app.models.Booking
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class PendingBookingsFragment : Fragment() {
    
    private var _binding: FragmentPendingBookingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var pendingBookingAdapter: PendingBookingAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        loadPendingBookings()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadPendingBookings()
        }
    }
    
    private fun setupRecyclerView() {
        pendingBookingAdapter = PendingBookingAdapter(
            bookings = emptyList(),
            onAcceptClick = { booking -> acceptBooking(booking) },
            onRejectClick = { booking -> rejectBooking(booking) },
            onViewProfileClick = { booking -> viewPassengerProfile(booking) }
        )
        binding.rvPendingBookings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pendingBookingAdapter
        }
    }
    
    private fun loadPendingBookings() {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) {
            showEmptyState()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPendingBookingsForDriver(driverId)
                if (response.isSuccessful && response.body() != null) {
                    val bookings = response.body()!!
                    if (bookings.isNotEmpty()) {
                        // Load ride details for each booking
                        loadRideDetailsForBookings(bookings)
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                }
            } catch (_: Exception) {
                showEmptyState()
                Toast.makeText(context, "Error loading pending bookings", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private suspend fun loadRideDetailsForBookings(bookings: List<Booking>) {
        val bookingsWithRides = bookings.toMutableList()
        for (booking in bookingsWithRides) {
            try {
                val rideResponse = RetrofitClient.apiService.getRideById(booking.rideId)
                if (rideResponse.isSuccessful && rideResponse.body() != null) {
                    booking.ride = rideResponse.body()
                }
            } catch (_: Exception) {
                // Ignore errors for individual ride lookups
            }
        }
        pendingBookingAdapter.updateBookings(bookingsWithRides)
        binding.rvPendingBookings.visibility = View.VISIBLE
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvPendingBookings.visibility = View.GONE
    }
    
    private fun viewPassengerProfile(booking: Booking) {
        val intent = Intent(requireContext(), PassengerProfileActivity::class.java)
        intent.putExtra("PASSENGER_ID", booking.passengerId)
        startActivity(intent)
    }

    private fun acceptBooking(booking: Booking) {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.acceptBooking(booking.id, driverId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Booking accepted!", Toast.LENGTH_SHORT).show()
                    loadPendingBookings()
                } else {
                    Toast.makeText(context, "Failed to accept booking", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                // Network error - show error message
                Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun rejectBooking(booking: Booking) {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.rejectBooking(booking.id, driverId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Booking rejected", Toast.LENGTH_SHORT).show()
                    loadPendingBookings()
                } else {
                    Toast.makeText(context, "Failed to reject booking", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                // Network error - show error message
                Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
