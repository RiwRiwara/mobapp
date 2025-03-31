package com.example.mobileappcar.ui.screens.register

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

class RegisterViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val messages: List<String>) : RegisterState() // Changed to handle multiple error messages
    }

    fun register(username: String, password: String, email: String, firstName: String, lastName: String, phone: String) {
        val validationErrors = validateInput(username, password, email, firstName, lastName, phone)
        if (validationErrors.isNotEmpty()) {
            _registerState.value = RegisterState.Error(validationErrors)
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            Log.d("RegisterViewModel", "Attempting registration for username: $username, email: $email")

            val result = apiRepository.registerUser(username, password, email, firstName, lastName, phone)
            result.onSuccess { user ->
                Log.i("RegisterViewModel", "API registration successful: ${user.username}")
                _registerState.value = RegisterState.Success(user)
            }.onFailure { exception ->
                val errorMessages = when (exception) {
                    is ApiException.HttpError -> {
                        when (exception.code) {
                            400 -> parseValidationErrors(exception.message) // Parse validation errors
                            401 -> listOf("Unauthorized access. Please try again.") // Shouldn't happen for register, but included for safety
                            500 -> listOf("Registration failed due to server error. Please try again later.")
                            else -> listOf("An unexpected error occurred. Please try again.")
                        }
                    }
                    is ApiException.NetworkError -> listOf("Network error. Please check your connection.")
                    else -> listOf("An unknown error occurred. Please try again.")
                }
                Log.e("RegisterViewModel", "API registration failed: ${exception.message}")
                _registerState.value = RegisterState.Error(errorMessages)
            }
        }
    }

    private fun validateInput(username: String, password: String, email: String, firstName: String, lastName: String, phone: String): List<String> {
        val errors = mutableListOf<String>()

        if (username.isEmpty()) errors.add("Username is required")
        else if (username.length < 3) errors.add("Username must be at least 3 characters")

        if (password.isEmpty()) errors.add("Password is required")
        else if (password.length < 6) errors.add("Password must be at least 6 characters")

        if (email.isEmpty()) errors.add("Email is required")
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) errors.add("Valid email is required")

        if (firstName.isEmpty()) errors.add("First name is required")

        if (lastName.isEmpty()) errors.add("Last name is required")

        if (phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) errors.add("Valid phone number is required")

        return errors
    }

    private fun parseValidationErrors(errorMessage: String?): List<String> {
        return try {
            // Assume the backend returns JSON like { "errors": [{ "msg": "Username is required" }, ...] } or { "error": "Username or email already exists" }
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap = gson.fromJson<Map<String, Any>>(errorMessage, type)

            when {
                errorMap.containsKey("errors") -> {
                    val errorsList = errorMap["errors"] as? List<Map<String, String>>
                    errorsList?.mapNotNull { it["msg"] } ?: listOf("Validation error occurred.")
                }
                errorMap.containsKey("error") -> listOf(errorMap["error"].toString())
                else -> listOf("Validation error occurred.")
            }
        } catch (e: Exception) {
            listOf("Invalid input. Please check your details.")
        }
    }
}