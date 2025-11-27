package com.carpooling.app.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // PASSENGER or DRIVER
    val token: String = ""
)
