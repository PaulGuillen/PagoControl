package com.example.androidbase.features.auth.ui.register
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val validationAttempted: Boolean = false,
    val loading: Boolean = false,
    val completed: Boolean = false,
    val demoOnly: Boolean = false,
    val error: RegisterError? = null,
)
