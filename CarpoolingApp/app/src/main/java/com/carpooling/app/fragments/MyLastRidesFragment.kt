package com.carpooling.app.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.R
import com.carpooling.app.adapters.LastRideAdapter
import com.carpooling.app.databinding.DialogReviewBinding
import com.carpooling.app.databinding.FragmentMyLastRidesBinding
import com.carpooling.app.models.Booking
import com.carpooling.app.models.CreateReviewRequest
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Fragment for passengers to see their confirmed (accepted) rides and leave reviews.
 * 
 * Shows only ACCEPTED bookings where the passenger can rate the driver.
 */
class MyLastRidesFragment : Fragment() {
    
    companion object {
        /** Number of characters to display when showing truncated ride ID */
        private const val RIDE_ID_DISPLAY_LENGTH = 8
    }
    
    private var _binding: FragmentMyLastRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var lastRideAdapter: LastRideAdapter
    
    // Track which rides the user has already reviewed
    private val reviewedRideIds = mutableSetOf<String>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLastRidesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        loadReviewedRides()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadReviewedRides()
        }
    }
    
    private fun setupRecyclerView() {
        lastRideAdapter = LastRideAdapter(
            bookings = emptyList(),
            reviewedRideIds = reviewedRideIds,
            onReviewClick = { booking ->
                showReviewDialog(booking)
            }
        )
        binding.rvLastRides.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lastRideAdapter
        }
    }
    
    private fun loadReviewedRides() {
        val passengerId = sessionManager.getUserId()
        if (passengerId.isEmpty()) {
            showEmptyState()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // Load bookings first
                val response = RetrofitClient.apiService.getPassengerBookings(passengerId)
                if (response.isSuccessful && response.body() != null) {
                    val bookings = response.body()!!
                    // Filter to only show ACCEPTED bookings (confirmed rides)
                    val confirmedBookings = bookings.filter { it.status == "ACCEPTED" }
                    
                    if (confirmedBookings.isNotEmpty()) {
                        // Load user's existing reviews for these rides
                        val confirmedRideIds = confirmedBookings.map { it.rideId }
                        loadUserReviews(passengerId, confirmedRideIds)
                        
                        // Load ride details for each booking
                        loadRideDetailsForBookings(confirmedBookings)
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                    Toast.makeText(context, "Failed to load rides", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showEmptyState()
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private suspend fun loadUserReviews(passengerId: String, confirmedRideIds: List<String>) {
        // Clear existing reviewed ride IDs
        reviewedRideIds.clear()
        
        // For each confirmed ride, check if the user has already reviewed it
        for (rideId in confirmedRideIds) {
            try {
                val reviewsResponse = RetrofitClient.apiService.getReviewsByRide(rideId)
                if (reviewsResponse.isSuccessful) {
                    val reviews = reviewsResponse.body() ?: emptyList()
                    // Check if this passenger has reviewed this ride
                    val hasReviewed = reviews.any { it.reviewerId == passengerId && it.type == "DRIVER" }
                    if (hasReviewed) {
                        reviewedRideIds.add(rideId)
                    }
                }
            } catch (e: Exception) {
                // If we can't check this ride, assume not reviewed
                e.printStackTrace()
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
        lastRideAdapter.updateBookings(bookingsWithRides, reviewedRideIds)
        binding.rvLastRides.visibility = View.VISIBLE
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvLastRides.visibility = View.GONE
    }
    
    private fun showReviewDialog(booking: Booking) {
        val dialogBinding = DialogReviewBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        val ride = booking.ride
        if (ride != null) {
            dialogBinding.tvRideInfo.text = "${ride.from} → ${ride.to} on ${ride.date}"
        } else {
            dialogBinding.tvRideInfo.text = "Ride #${booking.rideId.take(RIDE_ID_DISPLAY_LENGTH)}"
        }
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnSubmit.setOnClickListener {
            val rating = dialogBinding.ratingBar.rating.toInt()
            val comment = dialogBinding.etComment.text.toString().trim()
            
            if (rating < 1) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            dialog.dismiss()
            submitReview(booking, rating, comment)
        }
        
        dialog.show()
    }
    
    private fun submitReview(booking: Booking, rating: Int, comment: String) {
        val passengerId = sessionManager.getUserId()
        val driverId = booking.ride?.driverId ?: return
        
        lifecycleScope.launch {
            try {
                val request = CreateReviewRequest(
                    reviewerId = passengerId,
                    reviewedId = driverId,
                    rideId = booking.rideId,
                    rating = rating,
                    comment = comment,
                    type = "DRIVER"  // Passenger reviewing driver
                )
                
                val response = RetrofitClient.apiService.createReview(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.success_review), Toast.LENGTH_SHORT).show()
                    // Add to reviewed rides set and refresh
                    reviewedRideIds.add(booking.rideId)
                    loadReviewedRides()
                } else {
                    Toast.makeText(context, "Failed to submit review: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
