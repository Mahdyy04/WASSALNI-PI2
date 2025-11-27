package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carpooling.app.databinding.ActivitySignupBinding
import com.carpooling.app.models.SignupRequest
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding
    private lateinit var sessionManager: SessionManager
    private var selectedRole = "PASSENGER"
    private var selectedGender = "MALE"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupRoleSpinner()
        setupGenderSpinner()
        
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val phoneNumber = binding.etPhone.text.toString().trim()
            
            if (validateInput(email, password, phoneNumber)) {
                signupUser(email, password, phoneNumber, selectedRole, selectedGender)
            }
        }
        
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    private fun setupRoleSpinner() {
        val roles = arrayOf("Passenger", "Driver")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.spinnerRole.setAdapter(adapter)
        binding.spinnerRole.setText("Passenger", false)
        
        binding.spinnerRole.setOnItemClickListener { _, _, position, _ ->
            selectedRole = if (position == 0) "PASSENGER" else "DRIVER"
            // Show/hide driver-specific fields
            updateDriverFieldsVisibility()
        }
    }
    
    private fun updateDriverFieldsVisibility() {
        if (selectedRole == "DRIVER") {
            binding.driverFieldsLayout?.visibility = View.VISIBLE
        } else {
            binding.driverFieldsLayout?.visibility = View.GONE
        }
    }
    
    private fun setupGenderSpinner() {
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        binding.spinnerGender.setAdapter(adapter)
        binding.spinnerGender.setText("Male", false)
        
        binding.spinnerGender.setOnItemClickListener { _, _, position, _ ->
            selectedGender = if (position == 0) "MALE" else "FEMALE"
        }
    }
    
    private fun validateInput(email: String, password: String, phoneNumber: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_empty_field)
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_invalid_email)
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_empty_field)
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }
        if (phoneNumber.isEmpty()) {
            binding.etPhone.error = getString(R.string.error_empty_field)
            return false
        }
        return true
    }
    
    private fun signupUser(email: String, password: String, phoneNumber: String, role: String, gender: String) {
        binding.btnSignup.isEnabled = false
        
        // Get driver-specific fields if driver role selected
        val licenseNumber = if (role == "DRIVER") binding.etLicense?.text?.toString()?.trim() else null
        val vehicleNumber = if (role == "DRIVER") binding.etVehicleNumber?.text?.toString()?.trim() else null
        val vehiclePlate = if (role == "DRIVER") binding.etVehiclePlate?.text?.toString()?.trim() else null
        
        lifecycleScope.launch {
            try {
                val request = SignupRequest(
                    email = email,
                    password = password,
                    phoneNumber = phoneNumber,
                    gender = gender,
                    userType = role,
                    licenseNumber = licenseNumber,
                    vehicleNumber = vehicleNumber,
                    vehiclePlate = vehiclePlate,
                    preferredPaymentMethod = if (role == "PASSENGER") "CASH" else null
                )
                
                val response = RetrofitClient.apiService.signup(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUser(user)
                    Toast.makeText(this@SignupActivity, 
                        getString(R.string.success_signup), Toast.LENGTH_SHORT).show()
                    
                    // Go to login page to authenticate
                    Toast.makeText(this@SignupActivity, 
                        "Please login with your new account", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@SignupActivity, 
                        "Signup failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, 
                    "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSignup.isEnabled = true
            }
        }
    }
}
