package com.example.mobileappcar.ui.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.Booking
import com.example.mobileappcar.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class BookingDetailViewModel(private val bookingId: Int) : ViewModel() {
    private val apiRepository = ApiRepository()
    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Loading)
    val bookingState: StateFlow<BookingState> = _bookingState

    sealed class BookingState {
        object Loading : BookingState()
        data class Success(val booking: Booking) : BookingState()
        data class Error(val message: String) : BookingState()
    }

    init {
        fetchBookingDetails()
    }

    fun fetchBookingDetails() {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            val result = apiRepository.getBookingDetails(bookingId)
            result.onSuccess { booking ->
                _bookingState.value = BookingState.Success(booking)
            }.onFailure { exception ->
                _bookingState.value = BookingState.Error(exception.message ?: "Failed to fetch booking")
                Log.e("BookingDetailViewModel", "Fetch booking failed", exception)
            }
        }
    }

    suspend fun createPayment(
        bookingId: Int,
        amount: Float,
        paymentMethod: String,
        image: String
    ): Result<Payment> {
        val result = apiRepository.createPayment(bookingId, amount, paymentMethod, null, image)
        result.onSuccess { payment ->
            Log.d("BookingDetailViewModel", "Payment created: $payment")
            fetchBookingDetails()
        }.onFailure { exception ->
            Log.e("BookingDetailViewModel", "Payment creation failed", exception)
        }
        return result
    }
}