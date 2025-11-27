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
import com.carpooling.app.models.City
import com.carpooling.app.models.CreateBookingRequest
import com.carpooling.app.models.Ride
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SearchRidesFragment : Fragment() {
    
    private var _binding: FragmentSearchRidesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rideAdapter: RideAdapter
    private lateinit var sessionManager: SessionManager
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var showDriverRides = false
    
    companion object {
        private const val ARG_SHOW_DRIVER_RIDES = "show_driver_rides"
        
        fun newInstance(showDriverRides: Boolean = false): SearchRidesFragment {
            return SearchRidesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_DRIVER_RIDES, showDriverRides)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDriverRides = arguments?.getBoolean(ARG_SHOW_DRIVER_RIDES, false) ?: false
    }
    
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
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        setupDatePicker()
        
        if (showDriverRides) {
            // Hide search form and show driver's rides
            binding.searchCard.visibility = View.GONE
            loadDriverRides()
        } else {
            loadAllRides()
        }
        
        binding.btnSearch.setOnClickListener {
            val from = binding.etFrom.text.toString().trim()
            val to = binding.etTo.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
            
            if (from.isNotEmpty() || to.isNotEmpty() || date.isNotEmpty()) {
                searchRides(from, to, date)
            } else {
                Toast.makeText(context, "Please fill at least one field to search", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupRecyclerView() {
        rideAdapter = RideAdapter(emptyList()) { ride ->
            if (showDriverRides) {
                // For driver's own rides, show details or manage
                Toast.makeText(context, "Ride details: ${ride.from} → ${ride.to}", 
                    Toast.LENGTH_SHORT).show()
            } else {
                // For passengers, book the ride
                bookRide(ride)
            }
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
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
            }.show()
        }
    }
    
    private fun loadAllRides() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllRides()
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    if (rides.isNotEmpty()) {
                        rideAdapter.updateRides(rides)
                    } else {
                        loadSampleRides()
                    }
                } else {
                    loadSampleRides()
                }
            } catch (e: Exception) {
                loadSampleRides()
            }
        }
    }
    
    private fun loadDriverRides() {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) {
            loadSampleRides()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDriverRides(driverId)
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    if (rides.isNotEmpty()) {
                        rideAdapter.updateRides(rides)
                    } else {
                        Toast.makeText(context, "No rides published yet", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load your rides", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadSampleRides() {
        // Load sample rides for demo
        val sampleRides = listOf(
            Ride(
                id = "demo_1",
                departureCity = City("Tunis", "1000"),
                destinationCity = City("Sousse", "4000"),
                date = "2025-12-01",
                availableSeats = 3,
                pricePerSeat = 25.0,
                driverId = "driver_1"
            ),
            Ride(
                id = "demo_2",
                departureCity = City("Tunis", "1000"),
                destinationCity = City("Sfax", "3000"),
                date = "2025-12-01",
                availableSeats = 2,
                pricePerSeat = 30.0,
                driverId = "driver_2"
            ),
            Ride(
                id = "demo_3",
                departureCity = City("Sousse", "4000"),
                destinationCity = City("Monastir", "5000"),
                date = "2025-12-02",
                availableSeats = 4,
                pricePerSeat = 15.0,
                driverId = "driver_3"
            )
        )
        rideAdapter.updateRides(sampleRides)
    }
    
    private fun searchRides(from: String, to: String, date: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchRides(
                    departureCity = from.ifEmpty { null },
                    destinationCity = to.ifEmpty { null },
                    date = date.ifEmpty { null }
                )
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    if (rides.isNotEmpty()) {
                        rideAdapter.updateRides(rides)
                    } else {
                        Toast.makeText(context, getString(R.string.no_rides_found), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.no_rides_found), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun bookRide(ride: Ride) {
        val passengerId = sessionManager.getUserId()
        if (passengerId.isEmpty()) {
            Toast.makeText(context, "Please login to book a ride", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (ride.availableSeats <= 0) {
            Toast.makeText(context, "No seats available", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val request = CreateBookingRequest(
                    rideId = ride.id,
                    passengerId = passengerId,
                    seats = 1
                )
                val response = RetrofitClient.apiService.createBooking(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.success_booking), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Booking failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Demo mode - just show success
                Toast.makeText(context, getString(R.string.success_booking) + " (Demo)", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
