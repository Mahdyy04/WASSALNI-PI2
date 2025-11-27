package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.carpooling.app.databinding.ActivityDashboardBinding
import com.carpooling.app.fragments.SearchRidesFragment
import com.carpooling.app.utils.SessionManager
import com.google.android.material.tabs.TabLayout

class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        val user = sessionManager.getUser()
        binding.tvWelcome.text = getString(R.string.welcome) + ", ${user.name}!"
        
        setupTabs(user.role)
        
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(SearchRidesFragment())
        }
    }
    
    private fun setupTabs(role: String) {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Search Rides"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("My Bookings"))
        
        if (role == "DRIVER") {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Publish Ride"))
        }
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadFragment(SearchRidesFragment())
                    1 -> {
                        // Load My Bookings Fragment (to be created)
                        loadFragment(SearchRidesFragment()) // Placeholder
                    }
                    2 -> {
                        // Load Publish Ride Fragment (to be created)
                        loadFragment(SearchRidesFragment()) // Placeholder
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
