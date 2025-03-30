package com.example.mobileappcar.ui.screens.service

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.Booking
import com.example.mobileappcar.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _servicesState = MutableStateFlow<ServicesState>(ServicesState.Loading)
    val servicesState: StateFlow<ServicesState> = _servicesState

    private val _availableTimesState = MutableStateFlow<AvailableTimesState>(AvailableTimesState.Loading)
    val availableTimesState: StateFlow<AvailableTimesState> = _availableTimesState

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    sealed class ServicesState {
        object Loading : ServicesState()
        data class Success(val services: List<Service>) : ServicesState()
        data class Error(val message: String) : ServicesState()
    }

    sealed class AvailableTimesState {
        object Loading : AvailableTimesState()
        data class Success(val availableTimes: List<String>) : AvailableTimesState()
        data class Error(val message: String) : AvailableTimesState()
    }

    sealed class BookingState {
        object Idle : BookingState()
        object Loading : BookingState()
        data class Success(val booking: Booking) : BookingState()
        data class Error(val message: String) : BookingState()
    }

    init {
        fetchServices()
    }

    fun fetchServices() {
        viewModelScope.launch {
            _servicesState.value = ServicesState.Loading
            Log.d("ServiceViewModel", "Fetching services")
            val result = apiRepository.getServices()
            result.onSuccess { services ->
                Log.i("ServiceViewModel", "API fetched ${services.size} services")
                _servicesState.value = ServicesState.Success(services)
            }.onFailure { exception ->
                Log.e("ServiceViewModel", "API fetch failed: ${exception.message}")
                _servicesState.value = ServicesState.Error(exception.message ?: "Failed to fetch services")
            }
        }
    }

    fun fetchAvailableTimes(serviceId: Int) {
        viewModelScope.launch {
            _availableTimesState.value = AvailableTimesState.Loading
            Log.d("ServiceViewModel", "Fetching available times for service ID: $serviceId")
            val result = apiRepository.getServices() // Fetch all services and filter
            result.onSuccess { services ->
                val service = services.find { it.id == serviceId }
                if (service != null) {
                    val availableTimes = service.getAvailableTimes()
                    Log.i("ServiceViewModel", "Calculated ${availableTimes.size} available times")
                    _availableTimesState.value = AvailableTimesState.Success(availableTimes)
                } else {
                    _availableTimesState.value = AvailableTimesState.Error("Service not found")
                }
            }.onFailure { exception ->
                Log.e("ServiceViewModel", "Available times fetch failed: ${exception.message}")
                _availableTimesState.value = AvailableTimesState.Error(exception.message ?: "Failed to fetch available times")
            }
        }
    }

    fun createBooking(serviceId: Int, time: String, note: String? = null) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            Log.d("ServiceViewModel", "Creating booking for service ID: $serviceId, time: $time")
            val result = apiRepository.createBooking(serviceId, time, note)
            result.onSuccess { booking ->
                Log.i("ServiceViewModel", "Booking created successfully: ${booking.id}")
                _bookingState.value = BookingState.Success(booking)
            }.onFailure { exception ->
                Log.e("ServiceViewModel", "Booking creation failed: ${exception.message}")
                _bookingState.value = BookingState.Error(exception.message ?: "Failed to create booking")
            }
        }
    }
}