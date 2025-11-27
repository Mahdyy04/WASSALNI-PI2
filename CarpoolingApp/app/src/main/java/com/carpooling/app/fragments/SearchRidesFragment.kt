package com.carpooling.app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.R
import com.carpooling.app.adapters.RideAdapter
import com.carpooling.app.databinding.FragmentSearchRidesBinding
import com.carpooling.app.models.Ride
import com.carpooling.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SearchRidesFragment : Fragment() {
    
    private var _binding: FragmentSearchRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rideAdapter: RideAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRidesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupDatePicker()
        loadSampleRides()
        
        binding.btnSearch.setOnClickListener {
            val from = binding.etFrom.text.toString()
            val to = binding.etTo.text.toString()
            val date = binding.etDate.text.toString()
            
            if (from.isNotEmpty() && to.isNotEmpty() && date.isNotEmpty()) {
                searchRides(from, to, date)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupRecyclerView() {
        rideAdapter = RideAdapter(emptyList()) { ride ->
            Toast.makeText(context, "Booking ride: ${ride.from} → ${ride.to}", 
                Toast.LENGTH_SHORT).show()
            bookRide(ride.id)
        }
        binding.rvRides.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rideAdapter
        }
    }
    
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun loadSampleRides() {
        // Load sample rides for demo
        val sampleRides = listOf(
            Ride("1", "City A", "City B", "2025-12-01", "John Doe", 3, 25.0),
            Ride("2", "City A", "City B", "2025-12-01", "Jane Smith", 2, 30.0),
            Ride("3", "City C", "City D", "2025-12-02", "Bob Wilson", 4, 20.0)
        )
        rideAdapter.updateRides(sampleRides)
    }
    
    private fun searchRides(from: String, to: String, date: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchRides(from, to, date)
                if (response.isSuccessful && response.body() != null) {
                    rideAdapter.updateRides(response.body()!!)
                } else {
                    Toast.makeText(context, "No rides found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                // Keep showing sample rides on error
            }
        }
    }
    
    private fun bookRide(rideId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.bookRide(rideId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Ride booked successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Booking failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Demo mode - just show success
                Toast.makeText(context, "Ride booked successfully! (Demo)", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
