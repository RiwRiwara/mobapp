package com.example.mobileappcar.ui.screens.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    sealed class PaymentState {
        object Idle : PaymentState()
        object Loading : PaymentState()
        data class Success(val payment: Payment) : PaymentState()
        data class Error(val message: String) : PaymentState()
    }

    fun createPayment(bookingId: Int, amount: Float, paymentMethod: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            Log.d("PaymentViewModel", "Creating payment for booking ID: $bookingId")
            val result = apiRepository.createPayment(bookingId, amount, paymentMethod)
            result.onSuccess { payment ->
                Log.i("PaymentViewModel", "Payment created successfully: ${payment.id}")
                _paymentState.value = PaymentState.Success(payment)
            }.onFailure { exception ->
                Log.e("PaymentViewModel", "Payment creation failed: ${exception.message}")
                _paymentState.value = PaymentState.Error(exception.message ?: "Failed to create payment")
            }
        }
    }
}