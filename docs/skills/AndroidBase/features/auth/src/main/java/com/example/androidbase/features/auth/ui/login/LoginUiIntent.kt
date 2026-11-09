package com.example.androidbase.features.auth.ui.login

sealed interface LoginUiIntent {
    data class EmailChanged(val value: String) : LoginUiIntent
    data class PasswordChanged(val value: String) : LoginUiIntent
    data object TogglePassword : LoginUiIntent
    data object FillDemo : LoginUiIntent
    data object Submit : LoginUiIntent
}
