package com.carpooling.app.models

import com.google.gson.annotations.SerializedName

/**
 * User model matching backend's AppUser structure
 */
data class User(
    val id: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val gender: String = "", // MALE or FEMALE
    @SerializedName("userType")
    val role: String = "", // PASSENGER, DRIVER, or ADMIN
    val token: String = "",
    val isBanned: Boolean = false
) {
    // Convenience property to display a name (using email prefix if no name field)
    val displayName: String
        get() = email.substringBefore("@")
}
