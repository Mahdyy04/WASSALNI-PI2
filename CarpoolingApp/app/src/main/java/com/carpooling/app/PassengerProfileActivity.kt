package com.carpooling.app

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.adapters.ReviewAdapter
import com.carpooling.app.databinding.ActivityPassengerProfileBinding
import com.carpooling.app.models.Review
import com.carpooling.app.models.User
import com.carpooling.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

class PassengerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPassengerProfileBinding
    private lateinit var reviewAdapter: ReviewAdapter
    private var passengerId: String = ""

    companion object {
        private const val TAG = "PassengerProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassengerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Passenger Profile"

        // Get passenger ID from intent
        passengerId = intent.getStringExtra("PASSENGER_ID") ?: ""

        if (passengerId.isEmpty()) {
            Toast.makeText(this, "Invalid passenger ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadPassengerProfile()
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(this@PassengerProfileActivity)
            adapter = reviewAdapter
        }
    }

    private fun loadPassengerProfile() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE

        lifecycleScope.launch {
            try {
                // Load user info
                val userResponse = RetrofitClient.apiService.getUserById(passengerId)
                Log.d(TAG, "User response: ${userResponse.isSuccessful}")

                // Load average rating
                val ratingResponse = RetrofitClient.apiService.getAverageRating(passengerId)
                Log.d(TAG, "Rating response: ${ratingResponse.isSuccessful}")

                // Load reviews as passenger
                val reviewsResponse = RetrofitClient.apiService.getReviewsByUserAndType(
                    passengerId,
                    "PASSENGER"
                )
                Log.d(TAG, "Reviews response: ${reviewsResponse.isSuccessful}, body: ${reviewsResponse.body()?.size ?: 0} reviews")

                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val user = userResponse.body()!!
                    displayUserInfo(user)

                    // Display average rating
                    if (ratingResponse.isSuccessful && ratingResponse.body() != null) {
                        val avgRating = ratingResponse.body()!!.averageRating
                        binding.ratingBar.rating = avgRating.toFloat()
                        binding.tvRatingValue.text = String.format(Locale.US, "%.1f", avgRating)
                        Log.d(TAG, "Average rating set to: $avgRating")
                    } else {
                        binding.ratingBar.rating = 0f
                        binding.tvRatingValue.text = "N/A"
                        Log.d(TAG, "No rating data available")
                    }

                    // Display reviews
                    if (reviewsResponse.isSuccessful && reviewsResponse.body() != null) {
                        var reviews = reviewsResponse.body()!!
                        Log.d(TAG, "Displaying ${reviews.size} reviews")

                        // Load reviewer names
                        reviews = loadReviewerNames(reviews)

                        displayReviews(reviews)
                    } else {
                        Log.d(TAG, "Reviews response failed or empty")
                        binding.tvNoReviews.visibility = View.VISIBLE
                        binding.rvReviews.visibility = View.GONE
                    }

                    binding.contentLayout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        this@PassengerProfileActivity,
                        "Failed to load passenger profile",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "User response failed: ${userResponse.errorBody()}")
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PassengerProfileActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Exception: ${e.message}", e)
                finish()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun loadReviewerNames(reviews: List<Review>): List<Review> {
        return reviews.map { review ->
            if (review.reviewerName.isEmpty()) {
                try {
                    val userResponse = RetrofitClient.apiService.getUserById(review.reviewerId)
                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        val user = userResponse.body()!!
                        review.copy(reviewerName = user.displayName)
                    } else {
                        review
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading reviewer name: ${e.message}")
                    review
                }
            } else {
                review
            }
        }
    }

    private fun displayUserInfo(user: User) {
        binding.tvPassengerName.text = user.displayName
        binding.tvPassengerEmail.text = user.email
        binding.tvPassengerPhone.text = user.phoneNumber
        binding.tvPassengerGender.text = user.gender

        // Display user type badge
        binding.tvUserType.text = user.role
    }

    private fun displayReviews(reviews: List<Review>) {
        if (reviews.isEmpty()) {
            Log.d(TAG, "No reviews to display")
            binding.tvNoReviews.visibility = View.VISIBLE
            binding.rvReviews.visibility = View.GONE
            binding.tvReviewCount.text = "0 Review(s)"
        } else {
            Log.d(TAG, "Displaying ${reviews.size} reviews")
            binding.tvNoReviews.visibility = View.GONE
            binding.rvReviews.visibility = View.VISIBLE
            binding.tvReviewCount.text = "${reviews.size} Review(s)"
            reviewAdapter.updateReviews(reviews)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

