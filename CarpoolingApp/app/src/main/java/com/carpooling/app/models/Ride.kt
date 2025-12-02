package com.carpooling.app.models

import com.google.gson.annotations.SerializedName

/**
 * City model matching backend's City structure with geolocation support
 */
data class City(
    val name: String = "",
    val postalCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
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
    val driverId: String = ""
) {
    // Convenience properties for UI display
    val from: String get() = departureCity.name
    val to: String get() = destinationCity.name
    val driverName: String get() = "Driver" // Will be populated from a separate call if needed
    
    // Safe price accessor (defaults to 0.0 if null)
    val price: Double get() = pricePerSeat ?: 0.0
    
    // Check if the ride has geolocation data
    val hasGeolocation: Boolean get() = 
        departureCity.latitude != null && departureCity.longitude != null &&
        destinationCity.latitude != null && destinationCity.longitude != null
}