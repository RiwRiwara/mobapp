package com.example.mobileappcar.data.repository

import com.example.mobileappcar.model.*
import retrofit2.http.*

interface CarSpaApiService {
    @POST("api/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): UserResponse

    @POST("api/users/login")
    suspend fun loginUser(@Body request: LoginRequest): UserResponse

    @GET("api/services")
    suspend fun getServices(): List<Service>

    @GET("api/services/{id}/times")
    suspend fun getAvailableTimes(@Path("id") serviceId: Int): List<String>

    @GET("api/bookings")
    suspend fun getBookings(@Header("Authorization") token: String): List<Booking>

    @GET("api/bookings/{id}")
    suspend fun getBookingDetails(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Booking

    @POST("api/bookings")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body request: BookingRequest
    ): Booking

    @PATCH("api/bookings/{id}")
    suspend fun updateBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateBookingRequest
    ): Booking

    @POST("api/payments")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Body request: PaymentRequest
    ): Payment

    @GET("api/users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): User
}