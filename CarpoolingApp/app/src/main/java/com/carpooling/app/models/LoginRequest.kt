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
 * Response model for booking creation matching backend's BookingResponse
 */
data class BookingResponse(
    val bookingId: String = "",
    val rideId: String = "",
    val passengerId: String = "",
    val seatsBooked: Int = 1,
    val status: String = "PENDING"
)

/**
 * Booking model matching backend's Booking structure
 */
data class Booking(
    val id: String = "",
    val rideId: String = "",
    val passengerId: String = "",
    val seatsBooked: Int = 1,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, CANCELLED, COMPLETED
    // Extended info (for display purposes)
    var ride: Ride? = null
)

/**
 * Review model matching backend's Review structure
 */
data class Review(
    val id: String = "",
    val reviewerId: String = "",
    val reviewedId: String = "",
    val rideId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val type: String = "DRIVER" // DRIVER or PASSENGER
)

/**
 * Request model for creating a review matching backend's CreateReviewRequest
 */
data class CreateReviewRequest(
    val reviewerId: String,
    val reviewedId: String,
    val rideId: String,
    val rating: Int,
    val comment: String,
    val type: String // DRIVER or PASSENGER
)

/**
 * Response model for average rating
 */
data class AverageRatingResponse(
    val userId: String = "",
    val averageRating: Double = 0.0
)

/**
 * Report model matching backend's Report structure
 */
data class Report(
    val id: String = "",
    val reporterId: String = "",
    val reportedUserId: String = "",
    val rideId: String = "",
    val reason: String = "", // INAPPROPRIATE_BEHAVIOR, NO_SHOW, UNSAFE_DRIVING, OTHER
    val description: String = "",
    val status: String = "PENDING" // PENDING, REVIEWED, RESOLVED, DISMISSED
)

/**
 * Request model for creating a report matching backend's CreateReportRequest
 */
data class CreateReportRequest(
    val reporterId: String,
    val reportedUserId: String,
    val rideId: String,
    val reason: String, // INAPPROPRIATE_BEHAVIOR, NO_SHOW, UNSAFE_DRIVING, OTHER
    val description: String
)
