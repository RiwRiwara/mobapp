package com.example.mobileappcar.data.repository

import android.util.Log
import com.example.mobileappcar.model.Booking
import com.example.mobileappcar.model.Payment
import com.example.mobileappcar.model.Service
import com.example.mobileappcar.model.User
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface CarSpaApiService {
    @POST("/api/users/register")
    suspend fun registerUser(@Body request: RegisterRequest): UserResponse

    @POST("/api/users/login")
    suspend fun loginUser(@Body request: LoginRequest): UserResponse

    @GET("/api/services")
    suspend fun getServices(): List<Service>

    @GET("/api/bookings")
    suspend fun getBookings(@Header("Authorization") token: String): List<Booking>

    @GET("/api/bookings/{id}")
    suspend fun getBookingDetails(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Booking

    @POST("/api/bookings")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body request: BookingRequest
    ): Booking

    @PATCH("/api/bookings/{id}")
    suspend fun updateBooking(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateBookingRequest
    ): Booking

    @POST("/api/payments")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Body request: PaymentRequest
    ): Payment

    @GET("/api/users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): User
}

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val phone: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val token: String
)

data class BookingRequest(
    val service_id: Int,
    val date: String,
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
    val qr_code: String? = null,
    val image: String? = null // New field
)

class ApiRepository public constructor() {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/") // TODO: Make configurable for production
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: CarSpaApiService = retrofit.create(CarSpaApiService::class.java)

    companion object {
        @Volatile
        private var instance: ApiRepository? = null
        private var authToken: String? = null

        fun getInstance(): ApiRepository {
            return instance ?: synchronized(this) {
                instance ?: ApiRepository().also { instance = it }
            }
        }

        fun setAuthToken(token: String?) {
            authToken = token
            Log.d("ApiRepository", "Auth token set: $token")
        }

        fun getAuthToken(): String? = authToken

        fun clearAuthToken() {
            authToken = null
            Log.d("ApiRepository", "Auth token cleared")
        }
    }

    suspend fun registerUser(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(username, password, email, firstName, lastName, phone)
            val response = apiService.registerUser(request)
            setAuthToken("Bearer ${response.token}")
            Result.success(
                User(
                    id = response.id,
                    username = response.username,
                    password = "", // Password not returned
                    role = response.role,
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    email = response.email,
                    createdAt = "" // Not provided by API
                )
            )
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Register failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Registration failed: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Register failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Register failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun loginUser(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(username, password)
            val response = apiService.loginUser(request)
            setAuthToken("Bearer ${response.token}")
            Result.success(
                User(
                    id = response.id,
                    username = response.username,
                    password = "", // Password not returned
                    role = response.role,
                    firstName = "", // Fetch via getCurrentUser if needed
                    lastName = "",
                    phone = "",
                    email = response.email,
                    createdAt = "" // Not provided by API
                )
            )
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Login failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Login failed: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Login failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getServices(): Result<List<Service>> = withContext(Dispatchers.IO) {
        try {
            val services = apiService.getServices()
            Result.success(services)
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Get services failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to fetch services: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Get services failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get services failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getBookings(): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            getAuthToken()?.let { token ->
                val bookings = apiService.getBookings(token)
                Result.success(bookings)
            } ?: run {
                Log.e("ApiRepository", "Get bookings failed: No auth token")
                Result.failure(Exception("Please log in to view bookings"))
            }
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Get bookings failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to fetch bookings: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Get bookings failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get bookings failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getBookingDetails(bookingId: Int): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            getAuthToken()?.let { token ->
                val booking = apiService.getBookingDetails(token, bookingId)
                Result.success(booking)
            } ?: run {
                Log.e("ApiRepository", "Get booking details failed: No auth token")
                Result.failure(Exception("Please log in to view booking details"))
            }
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Get booking details failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to fetch booking details: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Get booking details failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get booking details failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createBooking(
        serviceId: Int,
        date: String,
        time: String,
        note: String? = null
    ): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            getAuthToken()?.let { token ->
                val request = BookingRequest(serviceId, date, time, note)
                val booking = apiService.createBooking(token, request)
                Result.success(booking)
            } ?: run {
                Log.e("ApiRepository", "Create booking failed: No auth token")
                Result.failure(Exception("Please log in to create a booking"))
            }
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Create booking failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to create booking: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Create booking failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Create booking failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateBooking(
        bookingId: Int,
        status: String
    ): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            getAuthToken()?.let { token ->
                val request = UpdateBookingRequest(status)
                val booking = apiService.updateBooking(token, bookingId, request)
                Result.success(booking)
            } ?: run {
                Log.e("ApiRepository", "Update booking failed: No auth token")
                Result.failure(Exception("Please log in to update a booking"))
            }
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Update booking failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to update booking: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Update booking failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Update booking failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createPayment(
        bookingId: Int,
        amount: Float,
        paymentMethod: String,
        qrCode: String? = null,
        image: String? = null
    ): Result<Payment> = withContext(Dispatchers.IO) {
        try {
            getAuthToken()?.let { token ->
                val request = PaymentRequest(bookingId, amount, paymentMethod, qrCode, image)
                val payment = apiService.createPayment(token, request)
                Log.i("ApiRepository", "Payment created: ${payment.id}")
                Result.success(payment)
            } ?: run {
                Log.e("ApiRepository", "Create payment failed: No auth token")
                Result.failure(Exception("Please log in to create a payment"))
            }
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Create payment failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to create payment: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Create payment failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Create payment failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            getAuthToken()?.let { token ->
                val user = apiService.getCurrentUser(token)
                Result.success(user)
            } ?: run {
                Log.e("ApiRepository", "Get current user failed: No auth token")
                Result.failure(Exception("Please log in to view your profile"))
            }
        } catch (e: HttpException) {
            Log.e("ApiRepository", "Get current user failed: ${e.code()} - ${e.message()}")
            Result.failure(Exception("Failed to fetch profile: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Log.e("ApiRepository", "Get current user failed: Network error - ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get current user failed: ${e.message}")
            Result.failure(e)
        }
    }
}