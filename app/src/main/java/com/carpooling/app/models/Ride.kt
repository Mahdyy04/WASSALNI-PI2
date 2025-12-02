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
    val pricePerSeat: Double? = null,
    val status: String = "SCHEDULED", // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    val driverId: String = "",
    var driverName: String = "" // Will be populated from API
) {
    // Convenience properties for UI display
    val from: String get() = departureCity.name
    val to: String get() = destinationCity.name

    // Safe price accessor (defaults to 0.0 if null)
    val price: Double get() = pricePerSeat ?: 0.0
}