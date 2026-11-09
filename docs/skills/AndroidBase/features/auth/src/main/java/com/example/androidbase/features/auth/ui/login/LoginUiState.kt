package com.example.androidbase.features.auth.ui.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val failure: LoginError? = null,
    val resetSent: Boolean = false,
)
