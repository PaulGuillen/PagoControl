package com.example.androidbase.domain

enum class AppFailure { INVALID_INPUT, INVALID_CREDENTIALS, NETWORK, UNAVAILABLE, UNKNOWN }

sealed interface AppResult<out Value> {
    data class Success<Value>(val value: Value) : AppResult<Value>
    data class Failure(val reason: AppFailure) : AppResult<Nothing>
}
