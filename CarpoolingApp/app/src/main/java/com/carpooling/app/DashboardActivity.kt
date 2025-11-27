package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.carpooling.app.databinding.ActivityDashboardBinding
import com.carpooling.app.fragments.MyBookingsFragment
import com.carpooling.app.fragments.MyRidesFragment
import com.carpooling.app.fragments.PendingBookingsFragment
import com.carpooling.app.fragments.PublishRideFragment
import com.carpooling.app.fragments.SearchRidesFragment
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import com.google.android.material.tabs.TabLayout

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
    
    private fun logout() {
        sessionManager.logout()
        RetrofitClient.setAuthToken(null)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun setupTabs(role: String) {
        binding.tabLayout.removeAllTabs()
        
        if (role == "DRIVER") {
            // Driver tabs: My Rides, Pending Requests, Publish Ride
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_rides))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.pending_requests))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.publish_ride))
        } else {
            // Passenger tabs: Search Rides, My Bookings
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.search_rides))
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.my_bookings))
        }
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (role == "DRIVER") {
                    when (tab?.position) {
                        0 -> loadFragment(MyRidesFragment())
                        1 -> loadFragment(PendingBookingsFragment())
                        2 -> loadFragment(PublishRideFragment())
                    }
                } else {
                    when (tab?.position) {
                        0 -> loadFragment(SearchRidesFragment())
                        1 -> loadFragment(MyBookingsFragment())
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
