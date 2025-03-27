package com.example.mobileappcar.data.remote

import com.example.mobileappcar.model.Booking
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("bookings")
    suspend fun getBookings(): List<Booking>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Int): Booking
}