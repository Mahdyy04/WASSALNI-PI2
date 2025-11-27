package com.carpooling.app.models

import com.google.gson.annotations.SerializedName

/**
 * City model matching backend's City structure
 */
data class City(
    val name: String = "",
    val postalCode: String = ""
)

/**
 * Ride model matching backend's Ride structure
 */
data class Ride(
    val id: String = "",
    val departureCity: City = City(),
    val destinationCity: City = City(),
    @SerializedName("departureDate")
    val date: String = "",
    val availableSeats: Int = 0,
    val pricePerSeat: Double = 0.0,
    val status: String = "SCHEDULED", // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    val driverId: String = ""
) {
    // Convenience properties for UI display
    val from: String get() = departureCity.name
    val to: String get() = destinationCity.name
    val driverName: String get() = "Driver" // Will be populated from a separate call if needed
}