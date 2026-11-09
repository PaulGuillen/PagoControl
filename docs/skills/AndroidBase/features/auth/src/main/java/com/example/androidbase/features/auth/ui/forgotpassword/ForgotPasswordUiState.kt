package com.example.androidbase.features.auth.ui.forgotpassword
data class ForgotPasswordUiState(
    val email: String = "",
    val loading: Boolean = false,
    val completed: Boolean = false,
    val demoOnly: Boolean = false,
    val error: ForgotPasswordError? = null,
)
