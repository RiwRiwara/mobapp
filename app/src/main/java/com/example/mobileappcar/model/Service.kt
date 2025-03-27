package com.example.mobileappcar.model

import com.google.gson.annotations.SerializedName

data class Service(
    val id: Int,
    val name: String,
    val description: String? = null, // Nullable since it’s optional
    val price: Float,
    val duration: Int, // In minutes
    val image: String? = null, // Nullable for optional image
    @SerializedName("created_at") val createdAt: String
)