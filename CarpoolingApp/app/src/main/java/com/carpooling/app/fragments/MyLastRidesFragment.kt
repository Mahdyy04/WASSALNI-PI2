package com.carpooling.app.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.R
import com.carpooling.app.adapters.LastRideAdapter
import com.carpooling.app.databinding.DialogReportBinding
import com.carpooling.app.databinding.DialogReviewBinding
import com.carpooling.app.databinding.FragmentMyLastRidesBinding
import com.carpooling.app.models.Booking
import com.carpooling.app.models.CreateReportRequest
import com.carpooling.app.models.CreateReviewRequest
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Fragment for passengers to see their confirmed (accepted) rides and leave reviews or reports.
 * 
 * Shows only ACCEPTED bookings where the passenger can rate or report the driver.
 */
class MyLastRidesFragment : Fragment() {
    
    companion object {
        /** Number of characters to display when showing truncated ride ID */
        private const val RIDE_ID_DISPLAY_LENGTH = 8
        
        /**
         * Report reason options matching backend ReportReason enum values.
         * These values must match the backend exactly: INAPPROPRIATE_BEHAVIOR, NO_SHOW, UNSAFE_DRIVING, OTHER
         */
        private val REPORT_REASONS = arrayOf(
            "INAPPROPRIATE_BEHAVIOR",
            "NO_SHOW",
            "UNSAFE_DRIVING",
            "OTHER"
        )
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
            },
            onReportClick = { booking ->
                showReportDialog(booking)
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
    
    private fun showReportDialog(booking: Booking) {
        val dialogBinding = DialogReportBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        val ride = booking.ride
        if (ride != null) {
            dialogBinding.tvRideInfo.text = "${ride.from} → ${ride.to} on ${ride.date}"
        } else {
            dialogBinding.tvRideInfo.text = "Ride #${booking.rideId.take(RIDE_ID_DISPLAY_LENGTH)}"
        }
        
        // Setup spinner with report reasons
        val reasonDisplayNames = arrayOf(
            getString(R.string.report_reason_inappropriate),
            getString(R.string.report_reason_no_show),
            getString(R.string.report_reason_unsafe_driving),
            getString(R.string.report_reason_other)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, reasonDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerReason.adapter = adapter
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnSubmit.setOnClickListener {
            val selectedPosition = dialogBinding.spinnerReason.selectedItemPosition
            val reason = REPORT_REASONS[selectedPosition]
            val description = dialogBinding.etDescription.text.toString().trim()
            
            dialog.dismiss()
            submitReport(booking, reason, description)
        }
        
        dialog.show()
    }
    
    private fun submitReport(booking: Booking, reason: String, description: String) {
        val passengerId = sessionManager.getUserId()
        val driverId = booking.ride?.driverId
        
        if (driverId == null) {
            Toast.makeText(context, getString(R.string.error_driver_info_unavailable), Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val request = CreateReportRequest(
                    reporterId = passengerId,
                    reportedUserId = driverId,
                    rideId = booking.rideId,
                    reason = reason,
                    description = description
                )
                
                val response = RetrofitClient.apiService.createReport(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.report_submitted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.error_submit_report), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
