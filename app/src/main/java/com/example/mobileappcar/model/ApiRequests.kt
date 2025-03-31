package com.example.mobileappcar.model
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val first_name: String,  // Match server’s snake_case
    val last_name: String,   // Match server’s snake_case
    val phone: String
)
data class LoginRequest(
    val username: String,
    val password: String
)


data class BookingRequest(
    val service_id: Int,
    val time: String,
    val note: String? = null
)
data class UpdateBookingRequest(
    val status: String
)

data class PaymentRequest(
    val booking_id: Int,
    val amount: Float,
    val payment_method: String,
    val qr_code: String?,
    val image: String?
)