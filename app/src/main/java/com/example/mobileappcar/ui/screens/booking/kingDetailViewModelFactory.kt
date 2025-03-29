package com.example.mobileappcar.ui.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BookingDetailViewModelFactory(private val bookingId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingDetailViewModel(bookingId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
