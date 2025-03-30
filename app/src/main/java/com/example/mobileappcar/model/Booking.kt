package com.example.mobileappcar.model

import com.google.gson.annotations.SerializedName

data class Booking(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("service_id") val serviceId: Int,
    val time: String, // Changed from time_slot_id, date, and time fields
    val status: String,
    @SerializedName("payment_id") val paymentId: Int? = null,
    val note: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("service_name") val serviceName: String? = null,
    val price: Float? = null,
    val amount: Float? = null,
    @SerializedName("payment_status") val paymentStatus: String? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("qr_code") val qrCode: String? = null,
    @SerializedName("payment_image") val paymentImage: String? = null
)