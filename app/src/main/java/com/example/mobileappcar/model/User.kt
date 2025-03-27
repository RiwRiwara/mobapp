package com.example.mobileappcar.model

import java.time.LocalDateTime

data class User(
    val id: Int,
    val username: String,
    val password: String, // Note: In production, this should not be stored in plain text in the app
    val role: String, // "customer", "staff", or "owner"
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String? = null, // Nullable since it’s optional in the DB
    val profilePicture: String? = null, // Nullable for optional profile picture
    val createdAt: String // Using String for simplicity; consider LocalDateTime with parsing if needed
)