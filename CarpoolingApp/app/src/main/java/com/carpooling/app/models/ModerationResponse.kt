package com.carpooling.app.models

data class ModerationResponse(
    val allowed: Boolean,
    val reason: String?
)
