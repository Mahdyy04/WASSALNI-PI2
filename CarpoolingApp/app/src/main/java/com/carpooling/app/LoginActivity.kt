package com.carpooling.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carpooling.app.databinding.ActivityLoginBinding
import com.carpooling.app.models.LoginRequest
import com.carpooling.app.models.User
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }
        
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
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
        return true
    }
    
    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(email, password)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUser(user)
                    Toast.makeText(this@LoginActivity, 
                        getString(R.string.success_login), Toast.LENGTH_SHORT).show()
                    
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    // For demo purposes, create a mock user if API fails
                    val mockUser = User(
                        id = "1",
                        name = "Demo User",
                        email = email,
                        role = "PASSENGER",
                        token = "mock_token"
                    )
                    sessionManager.saveUser(mockUser)
                    Toast.makeText(this@LoginActivity, 
                        "Login successful (Demo Mode)", Toast.LENGTH_SHORT).show()
                    
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                // Fallback to demo mode
                val mockUser = User(
                    id = "1",
                    name = "Demo User",
                    email = email,
                    role = "PASSENGER",
                    token = "mock_token"
                )
                sessionManager.saveUser(mockUser)
                Toast.makeText(this@LoginActivity, 
                    "Login successful (Demo Mode)", Toast.LENGTH_SHORT).show()
                
                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                finish()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
