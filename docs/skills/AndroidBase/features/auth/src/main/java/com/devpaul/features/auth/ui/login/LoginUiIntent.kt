package com.devpaul.features.auth.ui.login

sealed interface LoginUiIntent {
    data class Login(val email: String, val password: String) : LoginUiIntent
    data object ClearError : LoginUiIntent
}
