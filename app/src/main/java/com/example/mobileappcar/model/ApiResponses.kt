package com.example.mobileappcar.model
import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    @SerializedName("first_name") val first_name: String?,
    @SerializedName("last_name") val last_name: String?,
    val phone: String?,
    @SerializedName("created_at") val created_at: String?,
    val token: String
)