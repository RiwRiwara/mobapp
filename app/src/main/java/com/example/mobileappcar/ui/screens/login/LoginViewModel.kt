package com.example.mobileappcar.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.ApiException
import com.example.mobileappcar.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val messages: List<String>) : LoginState() // Changed to handle multiple error messages
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("LoginViewModel", "Attempting login for username: $username")

            val result = apiRepository.loginUser(username, password)
            result.onSuccess { user ->
                Log.i("LoginViewModel", "API login successful: ${user.username}")
                _loginState.value = LoginState.Success(user)
            }.onFailure { exception ->
                val errorMessages = when (exception) {
                    is ApiException.HttpError -> {
                        when (exception.code) {
                            400 -> parseValidationErrors(exception.message) // Parse validation errors (e.g., "Username is required")
                            401 -> listOf("Invalid credentials") // Authentication failure
                            500 -> listOf("Login failed due to server error. Please try again later.")
                            else -> listOf("An unexpected error occurred. Please try again.")
                        }
                    }
                    is ApiException.NetworkError -> listOf("Network error. Please check your connection.")
                    else -> listOf("An unknown error occurred. Please try again.")
                }
                Log.e("LoginViewModel", "API login failed: ${exception.message}")
                _loginState.value = LoginState.Error(errorMessages)
            }
        }
    }

    private fun parseValidationErrors(errorMessage: String?): List<String> {
        return try {
            // Assume the backend returns JSON like { "errors": [{ "msg": "Username is required" }, ...] }
            val gson = Gson()
            val type = object : TypeToken<Map<String, List<Map<String, String>>>>() {}.type
            val errorMap = gson.fromJson<Map<String, List<Map<String, String>>>>(errorMessage, type)
            errorMap["errors"]?.mapNotNull { it["msg"] } ?: listOf("Validation error occurred.")
        } catch (e: Exception) {
            listOf("Invalid input. Please check your details.")
        }
    }
}