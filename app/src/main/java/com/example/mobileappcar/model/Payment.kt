package com.example.mobileappcar.model

data class Payment(
    val id: Int,
    val bookingId: Int,
    val amount: Float,
    val status: String, // "pending", "completed"
    val paymentMethod: String, // "QR Code", "Credit Card", "Cash"
    val transactionId: String? = null,
    val qrCode: String? = null,
    val image: String? = null, // New field for payment slip image
    val createdAt: String
)