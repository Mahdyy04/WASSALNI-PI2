package com.carpooling.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.DriverProfileActivity
import com.carpooling.app.adapters.AdminRideAdapter
import com.carpooling.app.databinding.FragmentAdminRidesHistoryBinding
import com.carpooling.app.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Admin fragment to view all rides history in the system.
 */
class AdminRidesHistoryFragment : Fragment() {
    
    private var _binding: FragmentAdminRidesHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var rideAdapter: AdminRideAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRidesHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadRides()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadRides()
        }
    }
    
    private fun setupRecyclerView() {
        rideAdapter = AdminRideAdapter(
            rides = emptyList(),
            onViewDriverProfile = { driverId -> viewDriverProfile(driverId) }
        )
        binding.rvRides.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rideAdapter
        }
    }
    
    private fun viewDriverProfile(driverId: String) {
        val intent = Intent(requireContext(), DriverProfileActivity::class.java)
        intent.putExtra("DRIVER_ID", driverId)
        startActivity(intent)
    }
    
    private fun loadRides() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllRides()
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    
                    if (rides.isEmpty()) {
                        showEmptyState()
                    } else {
                        binding.tvRideCount.text = "Total rides: ${rides.size}"
                        rideAdapter.updateRides(rides)
                        binding.rvRides.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
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
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvRides.visibility = View.GONE
        binding.tvRideCount.text = "Total rides: 0"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
