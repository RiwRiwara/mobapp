package com.example.mobileappcar.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val apiRepository = ApiRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
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
                Log.e("LoginViewModel", "API login failed: ${exception.message}")
                _loginState.value = LoginState.Error(exception.message ?: "Invalid username or password")
            }
        }
    }
}