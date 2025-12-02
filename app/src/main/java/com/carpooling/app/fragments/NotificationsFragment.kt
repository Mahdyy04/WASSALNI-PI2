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
import com.carpooling.app.adapters.NotificationAdapter
import com.carpooling.app.databinding.FragmentNotificationsBinding
import com.carpooling.app.models.Booking
import com.carpooling.app.models.Notification
import com.carpooling.app.models.Ride
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * NotificationsFragment - Simplified notification system using existing booking data.
 * 
 * No separate backend notification service needed!
 * 
 * For DRIVERS: Shows pending booking requests (from getPendingBookingsForDriver)
 * For PASSENGERS: Shows recent booking status changes (ACCEPTED/REJECTED)
 * 
 * This uses client-side notification generation based on booking data.
 */
class NotificationsFragment : Fragment() {
    
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    
    // Cache for ride info to avoid repeated API calls
    private val rideCache = mutableMapOf<String, Ride>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        setupSwipeRefresh()
        
        // Hide mark all read button - not needed for client-side notifications
        binding.btnMarkAllRead.visibility = View.GONE
        
        loadNotifications()
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notifications) { _ ->
            // No-op: Client-side notifications don't need mark as read
        }
        
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadNotifications()
        }
    }
    
    private fun loadNotifications() {
        val user = sessionManager.getUser()
        val isDriver = user.role == "DRIVER"
        
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                notifications.clear()
                
                if (isDriver) {
                    // For drivers: Show pending booking requests
                    loadDriverNotifications(user.id)
                } else {
                    // For passengers: Show booking status changes
                    loadPassengerNotifications(user.id)
                }
                
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                notificationAdapter.notifyDataSetChanged()
                
                if (notifications.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvNotifications.visibility = View.VISIBLE
                }
                
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error loading notifications: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Load notifications for drivers - pending booking requests and new reviews
     */
    private suspend fun loadDriverNotifications(driverId: String) {
        // Load pending booking requests
        try {
            val response = RetrofitClient.apiService.getPendingBookingsForDriver(driverId)
            if (response.isSuccessful) {
                val pendingBookings = response.body() ?: emptyList()
                
                for (booking in pendingBookings) {
                    val ride = getRideInfo(booking.rideId)
                    val from = ride?.from ?: "Unknown"
                    val to = ride?.to ?: "Unknown"
                    
                    notifications.add(
                        Notification(
                            id = booking.id,
                            userId = driverId,
                            type = "BOOKING_REQUEST",
                            title = "New Booking Request",
                            message = "A passenger wants to book ${booking.seatsBooked} seat(s) for your ride from $from to $to. Go to Pending Requests to accept or reject.",
                            rideId = booking.rideId,
                            bookingId = booking.id,
                            isRead = false,
                            createdAt = ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Log but don't fail
            e.printStackTrace()
        }
        
        // Load reviews received by this driver
        try {
            val reviewResponse = RetrofitClient.apiService.getReviewsByUser(driverId)
            if (reviewResponse.isSuccessful) {
                val reviews = reviewResponse.body() ?: emptyList()
                
                for (review in reviews) {
                    val ride = getRideInfo(review.rideId)
                    val from = ride?.from ?: "Unknown"
                    val to = ride?.to ?: "Unknown"
                    
                    val stars = "⭐".repeat(review.rating)
                    val message = if (review.comment.isNotEmpty()) {
                        "You received a $stars rating for your ride from $from to $to. Comment: \"${review.comment}\""
                    } else {
                        "You received a $stars rating for your ride from $from to $to."
                    }
                    
                    notifications.add(
                        Notification(
                            id = review.id,
                            userId = driverId,
                            type = "REVIEW_RECEIVED",
                            title = "New Review Received ⭐",
                            message = message,
                            rideId = review.rideId,
                            bookingId = "",
                            isRead = true, // Reviews are shown as "read" since they're historical
                            createdAt = ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Load reports received by this driver
        try {
            val reportResponse = RetrofitClient.apiService.getReportsByReportedUser(driverId)
            if (reportResponse.isSuccessful) {
                val reports = reportResponse.body() ?: emptyList()
                
                for (report in reports) {
                    notifications.add(
                        Notification(
                            id = report.id,
                            userId = driverId,
                            type = "REPORT_RECEIVED",
                            title = getString(R.string.report_warning_title),
                            message = getString(R.string.report_warning_message),
                            rideId = report.rideId,
                            bookingId = "",
                            isRead = false, // Reports are shown as unread to get driver's attention
                            createdAt = ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Load cancelled bookings for driver's rides
        try {
            val ridesResponse = RetrofitClient.apiService.getDriverRides(driverId)
            if (ridesResponse.isSuccessful) {
                val rides = ridesResponse.body() ?: emptyList()
                
                for (ride in rides) {
                    try {
                        val bookingsResponse = RetrofitClient.apiService.getRideBookings(ride.id)
                        if (bookingsResponse.isSuccessful) {
                            val bookings = bookingsResponse.body() ?: emptyList()
                            
                            // Filter for cancelled bookings
                            val cancelledBookings = bookings.filter { it.status == "CANCELLED" }
                            
                            for (booking in cancelledBookings) {
                                notifications.add(
                                    Notification(
                                        id = booking.id,
                                        userId = driverId,
                                        type = "BOOKING_CANCELLED",
                                        title = getString(R.string.booking_cancelled_title),
                                        message = getString(
                                            R.string.booking_cancelled_message,
                                            booking.seatsBooked,
                                            ride.from,
                                            ride.to
                                        ),
                                        rideId = ride.id,
                                        bookingId = booking.id,
                                        isRead = true, // Cancelled bookings are shown as "read" since they're historical
                                        createdAt = ""
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Load notifications for passengers - booking status changes
     */
    private suspend fun loadPassengerNotifications(passengerId: String) {
        try {
            val response = RetrofitClient.apiService.getPassengerBookings(passengerId)
            if (response.isSuccessful) {
                val bookings = response.body() ?: emptyList()
                
                // Show ACCEPTED and REJECTED bookings as notifications
                for (booking in bookings) {
                    if (booking.status == "ACCEPTED" || booking.status == "REJECTED") {
                        val ride = getRideInfo(booking.rideId)
                        val from = ride?.from ?: "Unknown"
                        val to = ride?.to ?: "Unknown"
                        
                        val (type, title, message) = when (booking.status) {
                            "ACCEPTED" -> Triple(
                                "BOOKING_ACCEPTED",
                                "Booking Accepted! ✓",
                                "Your booking for the ride from $from to $to has been accepted by the driver."
                            )
                            "REJECTED" -> Triple(
                                "BOOKING_REJECTED",
                                "Booking Rejected",
                                "Your booking for the ride from $from to $to was rejected by the driver."
                            )
                            else -> continue
                        }
                        
                        notifications.add(
                            Notification(
                                id = booking.id,
                                userId = passengerId,
                                type = type,
                                title = title,
                                message = message,
                                rideId = booking.rideId,
                                bookingId = booking.id,
                                isRead = true, // Already processed bookings are shown as "read"
                                createdAt = ""
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get ride info with caching to avoid repeated API calls
     */
    private suspend fun getRideInfo(rideId: String): Ride? {
        // Check cache first
        rideCache[rideId]?.let { return it }
        
        return try {
            val response = RetrofitClient.apiService.getRideById(rideId)
            if (response.isSuccessful) {
                response.body()?.also { ride ->
                    rideCache[rideId] = ride
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
