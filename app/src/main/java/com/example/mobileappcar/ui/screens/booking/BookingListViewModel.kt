package com.example.mobileappcar.ui.screens.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingListViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _bookingsState = MutableStateFlow<BookingsState>(BookingsState.Loading)
    val bookingsState: StateFlow<BookingsState> = _bookingsState

    sealed class BookingsState {
        object Loading : BookingsState()
        data class Success(val bookings: List<Booking>) : BookingsState()
        data class Error(val message: String) : BookingsState()
    }

    init {
        fetchBookings()
    }

    fun fetchBookings() {
        viewModelScope.launch {
            _bookingsState.value = BookingsState.Loading
            Log.d("BookingListViewModel", "Fetching bookings")
            val result: Result<List<Booking>> = apiRepository.getBookings() // Explicitly type result// Explicitly type result
            result.onSuccess { bookings ->
                Log.i("BookingListViewModel", "API fetched ${bookings.size} bookings")
                _bookingsState.value = BookingsState.Success(bookings)
            }.onFailure { exception ->
                Log.e("BookingListViewModel", "API fetch failed: ${exception.message}")
                _bookingsState.value = BookingsState.Error(exception.message ?: "Failed to fetch bookings")
            }
        }
    }
}