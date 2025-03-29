package com.example.mobileappcar.model

sealed class ApiException(message: String) : Exception(message) {
    data class NetworkError(val error: String) : ApiException("Network error: $error")
    data class HttpError(val code: Int, val error: String) : ApiException("HTTP $code: $error")
    data class AuthError(val error: String) : ApiException("Authentication error: $error")
    data class UnknownError(val error: String) : ApiException("Unknown error: $error")
}