package com.example.mobileappcar.data.repository

import android.util.Log
import com.example.mobileappcar.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ApiRepository(baseUrl: String = "http://10.0.2.2:3000/") {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: CarSpaApiService = retrofit.create(CarSpaApiService::class.java)

    companion object {
        private var authToken: String? = null

        fun setAuthToken(token: String?) {
            authToken = token?.let { "Bearer $it" }
            Log.d("ApiRepository", "Auth token set: $authToken")
        }

        fun getAuthToken(): String? = authToken

        fun clearAuthToken() {
            authToken = null
            Log.d("ApiRepository", "Auth token cleared")
        }
    }

    private suspend fun <T> apiCall(call: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(call())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
            Log.e("ApiRepository", "HTTP Error: ${e.code()} - ${e.message()}, Body: $errorBody")
            Result.failure(ApiException.HttpError(e.code(), "HTTP ${e.code()}: ${e.message()} - $errorBody"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Network Error: ${e.message}")
            Result.failure(ApiException.NetworkError(e.message ?: "Unknown network error"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Unknown Error: ${e.message}")
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    private suspend fun <T> authenticatedApiCall(call: suspend (String) -> T): Result<T> = withContext(Dispatchers.IO) {
        getAuthToken()?.let { token ->
            apiCall { call(token) }
        } ?: Result.failure(ApiException.AuthError("No auth token available. Please log in."))
    }

    suspend fun registerUser(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<User> = apiCall {
        val request = RegisterRequest(username, password, email, firstName, lastName, phone)
        Log.d("ApiRepository", "Register request: $request")
        val response = apiService.registerUser(request)
        setAuthToken(response.token)
        User(
            id = response.id,
            username = response.username,
            email = response.email,
            role = response.role,
            firstName = response.first_name,  // Map server response to client model
            lastName = response.last_name,    // Map server response to client model
            phone = response.phone,
            createdAt = response.created_at,
            password = null
        )
    }

    suspend fun loginUser(username: String, password: String): Result<User> = apiCall {
        val request = LoginRequest(username, password)
        val response = apiService.loginUser(request)
        setAuthToken(response.token)
        User(
            id = response.id,
            username = response.username,
            email = response.email,
            role = response.role,
            firstName = response.first_name,
            lastName = response.last_name,
            phone = response.phone,
            createdAt = response.created_at,
            password = null
        )
    }

    suspend fun getServices(): Result<List<Service>> = apiCall {
        apiService.getServices()
    }

    suspend fun getAvailableTimes(serviceId: Int): Result<List<String>> = apiCall {
        apiService.getAvailableTimes(serviceId)
    }

    suspend fun getBookings(): Result<List<Booking>> = authenticatedApiCall { token ->
        apiService.getBookings(token)
    }

    suspend fun getBookingDetails(bookingId: Int): Result<Booking> = authenticatedApiCall { token ->
        apiService.getBookingDetails(token, bookingId)
    }

    suspend fun createBooking(
        serviceId: Int,
        time: String,
        note: String? = null
    ): Result<Booking> = authenticatedApiCall { token ->
        val request = BookingRequest(serviceId, time, note)
        apiService.createBooking(token, request)
    }

    suspend fun updateBooking(
        bookingId: Int,
        status: String
    ): Result<Booking> = authenticatedApiCall { token ->
        val request = UpdateBookingRequest(status)
        apiService.updateBooking(token, bookingId, request)
    }

    suspend fun createPayment(
        bookingId: Int,
        amount: Float,
        paymentMethod: String,
        qrCode: String? = null,
        image: String? = null
    ): Result<Payment> = authenticatedApiCall { token ->
        val request = PaymentRequest(bookingId, amount, paymentMethod, qrCode, image)
        Log.d("ApiRepository", "Sending payment request: $request")
        apiService.createPayment(token, request).also { payment ->
            Log.d("ApiRepository", "Payment response: $payment")
        }
    }

    suspend fun getCurrentUser(): Result<User> = authenticatedApiCall { token ->
        Log.d("ApiRepository", "Fetching current user with token: $token")
        apiService.getCurrentUser(token).also { user ->
            Log.d("ApiRepository", "Fetched user: ${user.username}")
        }
    }
}