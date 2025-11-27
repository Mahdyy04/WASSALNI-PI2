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
import com.carpooling.app.adapters.DriverRideAdapter
import com.carpooling.app.databinding.FragmentMyRidesBinding
import com.carpooling.app.models.Ride
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class MyRidesFragment : Fragment() {
    
    private var _binding: FragmentMyRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var rideAdapter: DriverRideAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRidesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        loadDriverRides()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadDriverRides()
        }
    }
    
    private fun setupRecyclerView() {
        rideAdapter = DriverRideAdapter(
            rides = emptyList(),
            onDeleteClick = { ride -> deleteRide(ride) }
        )
        binding.rvRides.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rideAdapter
        }
    }
    
    private fun loadDriverRides() {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) {
            showEmptyState()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDriverRides(driverId)
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    if (rides.isNotEmpty()) {
                        rideAdapter.updateRides(rides)
                        binding.rvRides.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
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
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvRides.visibility = View.GONE
    }
    
    private fun deleteRide(ride: Ride) {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteRide(ride.id, driverId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Ride deleted", Toast.LENGTH_SHORT).show()
                    loadDriverRides()
                } else {
                    Toast.makeText(context, "Failed to delete ride", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
