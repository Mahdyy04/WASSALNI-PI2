package com.carpooling.app.models

/**
 * Request model for user authentication
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response model for authentication that includes user and token
 */
data class AuthResponse(
    val user: User? = null,
    val token: String = "",
    val status: String = ""
)

/**
 * Request model for user registration matching backend's CreateAccountRequest
 */
data class SignupRequest(
    val email: String,
    val password: String,
    val phoneNumber: String,
    val gender: String, // MALE or FEMALE
    val userType: String, // PASSENGER, DRIVER
    // Driver-specific fields (optional)
    val licenseNumber: String? = null,
    val vehicleNumber: String? = null,
    val vehiclePlate: String? = null,
    // Passenger-specific fields (optional)
    val preferredPaymentMethod: String? = null
)

/**
 * Request model for creating a ride matching backend's CreateRideRequest
 */
data class CreateRideRequest(
    val departureCity: City,
    val destinationCity: City,
    val departureDate: String, // Format: yyyy-MM-dd
    val availableSeats: Int,
    val pricePerSeat: Double,
    val driverId: String
)

/**
 * Request model for booking a ride matching backend's CreateBookingRequest
 */
data class CreateBookingRequest(
    val rideId: String,
    val passengerId: String,
    val seats: Int = 1
)

/**
 * Booking model matching backend's Booking structure
 */
data class Booking(
    val id: String = "",
    val rideId: String = "",
    val passengerId: String = "",
    val seatsBooked: Int = 1,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, CANCELLED
    // Extended info (for display purposes)
    var ride: Ride? = null
)
