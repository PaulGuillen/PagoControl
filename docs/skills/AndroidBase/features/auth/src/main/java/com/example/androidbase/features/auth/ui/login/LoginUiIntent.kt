package com.example.androidbase.features.auth.ui.login

sealed interface LoginUiIntent {
    data class SignIn(val email: String, val password: String) : LoginUiIntent
    data class Register(val email: String, val password: String) : LoginUiIntent
    data class ResetPassword(val email: String) : LoginUiIntent
}
