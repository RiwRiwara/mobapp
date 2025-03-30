package com.example.mobileappcar.ui.screens.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    fun register(username: String, password: String, email: String, firstName: String, lastName: String, phone: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            Log.d("RegisterViewModel", "Attempting registration for username: $username, email: $email")

            val result = apiRepository.registerUser(username, password, email, firstName, lastName, phone)
            result.onSuccess { user ->
                Log.i("RegisterViewModel", "API registration successful: ${user.username}")
                _registerState.value = RegisterState.Success(user)
            }.onFailure { exception ->
                Log.e("RegisterViewModel", "API registration failed: ${exception.message}")
                _registerState.value = RegisterState.Error(exception.message ?: "Registration failed")
            }
        }
    }

    fun setError(message: String) {
        _registerState.value = RegisterState.Error(message)
    }
}