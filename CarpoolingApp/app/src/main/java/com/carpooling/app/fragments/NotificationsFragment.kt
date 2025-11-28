package com.carpooling.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.adapters.NotificationAdapter
import com.carpooling.app.databinding.FragmentNotificationsBinding
import com.carpooling.app.models.Notification
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {
    
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private lateinit var notificationAdapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    
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
        setupMarkAllReadButton()
        
        loadNotifications()
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notifications) { notification ->
            markAsRead(notification)
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
    
    private fun setupMarkAllReadButton() {
        binding.btnMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }
    
    private fun loadNotifications() {
        val user = sessionManager.getUser()
        
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getNotifications(user.id)
                
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                if (response.isSuccessful) {
                    val notificationList = response.body() ?: emptyList()
                    notifications.clear()
                    notifications.addAll(notificationList)
                    notificationAdapter.notifyDataSetChanged()
                    
                    if (notifications.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvNotifications.visibility = View.GONE
                        binding.btnMarkAllRead.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvNotifications.visibility = View.VISIBLE
                        // Show mark all read button only if there are unread notifications
                        val hasUnread = notifications.any { !it.isRead }
                        binding.btnMarkAllRead.visibility = if (hasUnread) View.VISIBLE else View.GONE
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun markAsRead(notification: Notification) {
        if (notification.isRead) return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.markNotificationAsRead(notification.id)
                if (response.isSuccessful) {
                    // Update local state
                    val index = notifications.indexOfFirst { it.id == notification.id }
                    if (index != -1) {
                        notifications[index] = notification.copy(isRead = true)
                        notificationAdapter.notifyItemChanged(index)
                        
                        // Update mark all read button visibility
                        val hasUnread = notifications.any { !it.isRead }
                        binding.btnMarkAllRead.visibility = if (hasUnread) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }
    
    private fun markAllAsRead() {
        val user = sessionManager.getUser()
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.markAllNotificationsAsRead(user.id)
                if (response.isSuccessful) {
                    // Update all local notifications to read
                    for (i in notifications.indices) {
                        notifications[i] = notifications[i].copy(isRead = true)
                    }
                    notificationAdapter.notifyDataSetChanged()
                    binding.btnMarkAllRead.visibility = View.GONE
                    Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to mark all as read", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
