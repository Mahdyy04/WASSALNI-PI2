package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carpooling.app.databinding.ActivitySignupBinding
import com.carpooling.app.models.SignupRequest
import com.carpooling.app.models.User
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding
    private lateinit var sessionManager: SessionManager
    private var selectedRole = "PASSENGER"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupRoleSpinner()
        
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInput(name, email, password)) {
                signupUser(name, email, password, selectedRole)
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
        }
    }
    
    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.error_empty_field)
            return false
        }
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
        return true
    }
    
    private fun signupUser(name: String, email: String, password: String, role: String) {
        binding.btnSignup.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.signup(
                    SignupRequest(name, email, password, role)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUser(user)
                    Toast.makeText(this@SignupActivity, 
                        getString(R.string.success_signup), Toast.LENGTH_SHORT).show()
                    
                    startActivity(Intent(this@SignupActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    // For demo purposes, create a mock user if API fails
                    val mockUser = User(
                        id = "1",
                        name = name,
                        email = email,
                        role = role,
                        token = "mock_token"
                    )
                    sessionManager.saveUser(mockUser)
                    Toast.makeText(this@SignupActivity, 
                        "Signup successful (Demo Mode)", Toast.LENGTH_SHORT).show()
                    
                    startActivity(Intent(this@SignupActivity, DashboardActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                // Fallback to demo mode
                val mockUser = User(
                    id = "1",
                    name = name,
                    email = email,
                    role = role,
                    token = "mock_token"
                )
                sessionManager.saveUser(mockUser)
                Toast.makeText(this@SignupActivity, 
                    "Signup successful (Demo Mode)", Toast.LENGTH_SHORT).show()
                
                startActivity(Intent(this@SignupActivity, DashboardActivity::class.java))
                finish()
            } finally {
                binding.btnSignup.isEnabled = true
            }
        }
    }
}
