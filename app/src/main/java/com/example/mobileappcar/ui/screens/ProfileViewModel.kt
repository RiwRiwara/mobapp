package com.example.mobileappcar.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val apiRepository = ApiRepository.getInstance()

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    sealed class UserState {
        object Loading : UserState()
        data class Success(val user: User) : UserState()
        data class Error(val message: String) : UserState()
    }

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() { // Made public so it can be called manually (e.g., retry)
        viewModelScope.launch {
            _userState.value = UserState.Loading
            Log.d("ProfileViewModel", "Fetching user profile")
            val result = apiRepository.getCurrentUser()
            result.onSuccess { user ->
                Log.i("ProfileViewModel", "API fetched user: ${user.username}")
                _userState.value = UserState.Success(user)
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "API fetch failed: ${exception.message}")
                _userState.value = UserState.Error(exception.message ?: "Failed to fetch profile")
            }
        }
    }
}