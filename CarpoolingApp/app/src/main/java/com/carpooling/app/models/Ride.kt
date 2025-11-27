package com.carpooling.app.models

data class Ride(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val date: String = "",
    val driverName: String = "",
    val availableSeats: Int = 0,
    val pricePerSeat: Double = 0.0
)