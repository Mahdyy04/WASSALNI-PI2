package com.carpooling.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.R
import com.carpooling.app.adapters.BookingAdapter
import com.carpooling.app.databinding.FragmentMyBookingsBinding
import com.carpooling.app.models.Booking
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class MyBookingsFragment : Fragment() {
    
    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var bookingAdapter: BookingAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        loadBookings()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadBookings()
        }
    }
    
    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(emptyList()) { booking ->
            cancelBooking(booking)
        }
        binding.rvBookings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookingAdapter
        }
    }
    
    private fun loadBookings() {
        val passengerId = sessionManager.getUserId()
        if (passengerId.isEmpty()) {
            showEmptyState()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPassengerBookings(passengerId)
                if (response.isSuccessful && response.body() != null) {
                    val bookings = response.body()!!
                    if (bookings.isNotEmpty()) {
                        // Load ride details for each booking
                        loadRideDetailsForBookings(bookings)
                    } else {
                        showEmptyState()
                    }
                } else {
                    showDemoBookings()
                }
            } catch (e: Exception) {
                showDemoBookings()
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
            } catch (e: Exception) {
                // Ignore errors for individual ride lookups
            }
        }
        bookingAdapter.updateBookings(bookingsWithRides)
        binding.rvBookings.visibility = View.VISIBLE
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvBookings.visibility = View.GONE
    }
    
    private fun showDemoBookings() {
        // Demo bookings for testing
        val demoBookings = listOf(
            Booking(
                id = "demo_booking_1",
                rideId = "demo_ride_1",
                passengerId = sessionManager.getUserId(),
                seatsBooked = 1,
                status = "PENDING"
            ),
            Booking(
                id = "demo_booking_2",
                rideId = "demo_ride_2",
                passengerId = sessionManager.getUserId(),
                seatsBooked = 2,
                status = "ACCEPTED"
            )
        )
        bookingAdapter.updateBookings(demoBookings)
        binding.rvBookings.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
    }
    
    private fun cancelBooking(booking: Booking) {
        val passengerId = sessionManager.getUserId()
        if (passengerId.isEmpty()) {
            Toast.makeText(context, "Error: Not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelBooking(booking.id, passengerId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                    loadBookings()
                } else {
                    Toast.makeText(context, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Demo mode
                Toast.makeText(context, "Booking cancelled (Demo)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
