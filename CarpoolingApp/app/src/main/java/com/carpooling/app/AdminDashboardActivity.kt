package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.carpooling.app.databinding.ActivityAdminDashboardBinding
import com.carpooling.app.fragments.AdminManageReportsFragment
import com.carpooling.app.fragments.AdminManageUsersFragment
import com.carpooling.app.fragments.AdminRidesHistoryFragment
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import com.google.android.material.tabs.TabLayout

/**
 * Admin Dashboard Activity - provides admin-only functionality.
 * 
 * Accessible only to users with role "ADMIN".
 * Features:
 * - View All Rides History
 * - Manage Reports (review reports and ban users)
 * - Manage Users (view all users, ban/unban)
 */
class AdminDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        val user = sessionManager.getUser()
        
        // Verify admin role
        if (user.role != "ADMIN") {
            // Redirect non-admin users to regular dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }
        
        binding.tvWelcome.text = getString(R.string.welcome) + ", Admin ${user.displayName}!"
        
        setupTabs()
        
        binding.btnLogout.setOnClickListener {
            logout()
        }
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(AdminRidesHistoryFragment())
        }
    }
    
    private fun logout() {
        sessionManager.logout()
        RetrofitClient.setAuthToken(null)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun setupTabs() {
        binding.tabLayout.removeAllTabs()
        
        // Admin tabs: Rides History, Manage Reports, Manage Users
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.admin_rides_history))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.admin_manage_reports))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.admin_manage_users))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadFragment(AdminRidesHistoryFragment())
                    1 -> loadFragment(AdminManageReportsFragment())
                    2 -> loadFragment(AdminManageUsersFragment())
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
