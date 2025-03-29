package com.example.mobileappcar.model


import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    val phone: String?,
    @SerializedName("created_at") val createdAt: String?,
    val password: String? = null
)