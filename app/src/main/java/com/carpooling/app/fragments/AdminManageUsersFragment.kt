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
import com.carpooling.app.adapters.AdminUserAdapter
import com.carpooling.app.databinding.FragmentAdminManageUsersBinding
import com.carpooling.app.models.User
import com.carpooling.app.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Admin fragment to manage all users (view, ban, unban).
 */
class AdminManageUsersFragment : Fragment() {
    
    private var _binding: FragmentAdminManageUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var userAdapter: AdminUserAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManageUsersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadUsers()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadUsers()
        }
    }
    
    private fun setupRecyclerView() {
        userAdapter = AdminUserAdapter(
            users = emptyList(),
            onToggleBanClick = { user ->
                if (user.isBanned) {
                    showUnbanConfirmationDialog(user)
                } else {
                    showBanConfirmationDialog(user)
                }
            }
        )
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }
    
    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!
                    // Filter out ADMIN users (don't show admin accounts in the list)
                    val nonAdminUsers = users.filter { it.role != "ADMIN" }
                    
                    if (nonAdminUsers.isEmpty()) {
                        showEmptyState()
                    } else {
                        val bannedCount = nonAdminUsers.count { it.isBanned }
                        binding.tvUserCount.text = "Total users: ${nonAdminUsers.size} (Banned: $bannedCount)"
                        userAdapter.updateUsers(nonAdminUsers)
                        binding.rvUsers.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                    }
                } else {
                    showEmptyState()
                    Toast.makeText(context, "Failed to load users", Toast.LENGTH_SHORT).show()
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
    
    private fun showBanConfirmationDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.admin_ban_confirm_title)
            .setMessage(getString(R.string.admin_ban_user_confirm, user.email))
            .setPositiveButton(R.string.admin_ban_user) { _, _ ->
                banUser(user)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showUnbanConfirmationDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.admin_unban_confirm_title)
            .setMessage(getString(R.string.admin_unban_user_confirm, user.email))
            .setPositiveButton(R.string.admin_unban_user) { _, _ ->
                unbanUser(user)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun banUser(user: User) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.banUser(user.id)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.admin_user_banned), Toast.LENGTH_SHORT).show()
                    loadUsers() // Refresh the list
                } else {
                    Toast.makeText(context, getString(R.string.admin_ban_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun unbanUser(user: User) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.unbanUser(user.id)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.admin_user_unbanned), Toast.LENGTH_SHORT).show()
                    loadUsers() // Refresh the list
                } else {
                    Toast.makeText(context, getString(R.string.admin_unban_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvUsers.visibility = View.GONE
        binding.tvUserCount.text = "Total users: 0"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
