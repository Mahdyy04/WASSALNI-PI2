package com.carpooling.app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.R
import com.carpooling.app.adapters.RideAdapter
import com.carpooling.app.databinding.FragmentSearchRidesBinding
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
    private var selectedGenderFilter: String? = null
    
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
        setupGenderFilter()
        loadAllRides()
        
        binding.btnSearch.setOnClickListener {
            val from = binding.etFrom.text.toString().trim()
            val to = binding.etTo.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
            
            searchRides(from, to, date)
        }
    }
    
    private fun setupRecyclerView() {
        rideAdapter = RideAdapter(emptyList()) { ride ->
            bookRide(ride)
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
    
    private fun setupGenderFilter() {
        val genders = arrayOf("Any", "Male Driver", "Female Driver")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.spinnerGenderFilter.setAdapter(adapter)
        binding.spinnerGenderFilter.setText("Any", false)
        
        binding.spinnerGenderFilter.setOnItemClickListener { _, _, position, _ ->
            selectedGenderFilter = when (position) {
                1 -> "MALE"
                2 -> "FEMALE"
                else -> null
            }
        }
    }
    
    private fun loadAllRides() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllRides()
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    if (rides.isNotEmpty()) {
                        rideAdapter.updateRides(rides)
                        binding.tvNoResults.visibility = View.GONE
                    } else {
                        binding.tvNoResults.visibility = View.VISIBLE
                    }
                } else {
                    binding.tvNoResults.visibility = View.VISIBLE
                    Toast.makeText(context, "Failed to load rides", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.tvNoResults.visibility = View.VISIBLE
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun searchRides(from: String, to: String, date: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchRides(
                    departureCity = from.ifEmpty { null },
                    destinationCity = to.ifEmpty { null },
                    date = date.ifEmpty { null }
                )
                if (response.isSuccessful && response.body() != null) {
                    val rides = response.body()!!
                    
                    // Note: Gender filter would require backend support to filter by driver gender
                    // Currently shows all matching rides
                    
                    if (rides.isNotEmpty()) {
                        rideAdapter.updateRides(rides)
                        binding.tvNoResults.visibility = View.GONE
                    } else {
                        rideAdapter.updateRides(emptyList())
                        binding.tvNoResults.visibility = View.VISIBLE
                        Toast.makeText(context, getString(R.string.no_rides_found), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.tvNoResults.visibility = View.VISIBLE
                    Toast.makeText(context, getString(R.string.no_rides_found), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.tvNoResults.visibility = View.VISIBLE
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
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
                    // Refresh rides to show updated seat count
                    loadAllRides()
                } else {
                    Toast.makeText(context, "Booking failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
