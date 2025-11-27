package com.carpooling.app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.carpooling.app.R
import com.carpooling.app.databinding.FragmentPublishRideBinding
import com.carpooling.app.models.City
import com.carpooling.app.models.CreateRideRequest
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PublishRideFragment : Fragment() {
    
    private var _binding: FragmentPublishRideBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishRideBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupDatePicker()
        
        binding.btnPublish.setOnClickListener {
            if (validateInput()) {
                publishRide()
            }
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
    
    private fun validateInput(): Boolean {
        val departureCity = binding.etDepartureCity.text.toString().trim()
        val destinationCity = binding.etDestinationCity.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val seatsText = binding.etSeats.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        
        if (departureCity.isEmpty()) {
            binding.etDepartureCity.error = getString(R.string.error_empty_field)
            return false
        }
        if (destinationCity.isEmpty()) {
            binding.etDestinationCity.error = getString(R.string.error_empty_field)
            return false
        }
        if (date.isEmpty()) {
            binding.etDate.error = getString(R.string.error_empty_field)
            return false
        }
        if (seatsText.isEmpty()) {
            binding.etSeats.error = getString(R.string.error_empty_field)
            return false
        }
        val seats = seatsText.toIntOrNull()
        if (seats == null || seats < 1 || seats > 8) {
            binding.etSeats.error = "Enter a valid number (1-8)"
            return false
        }
        if (priceText.isEmpty()) {
            binding.etPrice.error = getString(R.string.error_empty_field)
            return false
        }
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.etPrice.error = "Enter a valid price"
            return false
        }
        
        return true
    }
    
    private fun publishRide() {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) {
            Toast.makeText(context, "Error: Not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnPublish.isEnabled = false
        
        val departureCity = City(
            name = binding.etDepartureCity.text.toString().trim(),
            postalCode = binding.etDeparturePostal.text.toString().trim()
        )
        val destinationCity = City(
            name = binding.etDestinationCity.text.toString().trim(),
            postalCode = binding.etDestinationPostal.text.toString().trim()
        )
        
        val request = CreateRideRequest(
            departureCity = departureCity,
            destinationCity = destinationCity,
            departureDate = binding.etDate.text.toString().trim(),
            availableSeats = binding.etSeats.text.toString().toInt(),
            pricePerSeat = binding.etPrice.text.toString().toDouble(),
            driverId = driverId
        )
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.publishRide(request)
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(context, getString(R.string.success_publish), Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    Toast.makeText(context, "Failed to publish ride: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnPublish.isEnabled = true
            }
        }
    }
    
    private fun clearForm() {
        binding.etDepartureCity.setText("")
        binding.etDeparturePostal.setText("")
        binding.etDestinationCity.setText("")
        binding.etDestinationPostal.setText("")
        binding.etDate.setText("")
        binding.etSeats.setText("")
        binding.etPrice.setText("")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
