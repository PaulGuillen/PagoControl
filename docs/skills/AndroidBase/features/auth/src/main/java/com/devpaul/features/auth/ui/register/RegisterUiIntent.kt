package com.devpaul.features.auth.ui.register

sealed interface RegisterUiIntent {
    data class Register(
        val name: String,
        val email: String,
        val password: String,
    ) : RegisterUiIntent

    data object ClearError : RegisterUiIntent
}
