package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.carpooling.app.databinding.ActivityDashboardBinding
import com.carpooling.app.fragments.MyBookingsFragment
import com.carpooling.app.fragments.MyLastRidesFragment
import com.carpooling.app.fragments.MyRidesFragment
import com.carpooling.app.fragments.NotificationsFragment
import com.carpooling.app.fragments.PendingBookingsFragment
import com.carpooling.app.fragments.PublishRideFragment
import com.carpooling.app.fragments.SearchRidesFragment
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private var userRole: String = "PASSENGER"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        val user = sessionManager.getUser()
        userRole = user.role
        binding.tvWelcome.text = getString(R.string.welcome) + ", ${user.displayName}!"
        
        setupTabs(userRole)
        setupNotificationButton()
        
        binding.btnLogout.setOnClickListener {
            logout()
        }
        
        // Load default fragment based on role
        if (savedInstanceState == null) {
            if (userRole == "DRIVER") {
                loadFragment(MyRidesFragment())
            } else {
                loadFragment(SearchRidesFragment())
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }
    
    private fun logout() {
        sessionManager.logout()
        RetrofitClient.setAuthToken(null)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun setupNotificationButton() {
        binding.btnNotifications.setOnClickListener {
            loadFragment(NotificationsFragment())
            // Deselect all tabs when viewing notifications
            binding.tabLayout.clearOnTabSelectedListeners()
            for (i in 0 until binding.tabLayout.tabCount) {
                binding.tabLayout.getTabAt(i)?.view?.isSelected = false
            }
            setupTabListener()
        }
    }
    
    /**
     * Update notification badge using existing booking data (no separate notification service needed).
     * 
     * For DRIVERS: Count pending booking requests
     * For PASSENGERS: Count bookings with ACCEPTED/REJECTED status (simplified: always show bell, no count)
     */
    private fun updateNotificationBadge() {
        val user = sessionManager.getUser()
        val isDriver = user.role == "DRIVER"
        
        lifecycleScope.launch {
            try {
                if (isDriver) {
                    // For drivers: count pending bookings as notifications
                    val response = RetrofitClient.apiService.getPendingBookingsForDriver(user.id)
                    if (response.isSuccessful) {
                        val count = response.body()?.size ?: 0
                        runOnUiThread {
                            if (count > 0) {
                                binding.tvNotificationBadge.visibility = View.VISIBLE
                                binding.tvNotificationBadge.text = if (count > 9) "9+" else count.toString()
                            } else {
                                binding.tvNotificationBadge.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    // For passengers: check for ACCEPTED/REJECTED bookings
                    val response = RetrofitClient.apiService.getPassengerBookings(user.id)
                    if (response.isSuccessful) {
                        val bookings = response.body() ?: emptyList()
                        val notificationCount = bookings.count { 
                            it.status == "ACCEPTED" || it.status == "REJECTED" 
                        }
                        runOnUiThread {
                            if (notificationCount > 0) {
                                binding.tvNotificationBadge.visibility = View.VISIBLE
                                binding.tvNotificationBadge.text = if (notificationCount > 9) "9+" else notificationCount.toString()
                            } else {
                                binding.tvNotificationBadge.visibility = View.GONE
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail - notification badge is not critical
            }
        }
    }
    
    private fun setupTabs(role: String) {
        binding.tabLayout.removeAllTabs()
        
        if (role == "DRIVER") {
            // Driver tabs: My Rides, Pending Requests, Publish Ride
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_rides))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.pending_requests))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.publish_ride))
        } else {
            // Passenger tabs: Search Rides, My Bookings, My Last Rides
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.search_rides))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_bookings))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_last_rides))
        }
        
        setupTabListener()
    }
    
    private fun setupTabListener() {
        binding.tabLayout.clearOnTabSelectedListeners()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (userRole == "DRIVER") {
                    when (tab?.position) {
                        0 -> loadFragment(MyRidesFragment())
                        1 -> loadFragment(PendingBookingsFragment())
                        2 -> loadFragment(PublishRideFragment())
                    }
                } else {
                    when (tab?.position) {
                        0 -> loadFragment(SearchRidesFragment())
                        1 -> loadFragment(MyBookingsFragment())
                        2 -> loadFragment(MyLastRidesFragment())
                    }
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
