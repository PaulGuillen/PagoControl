package com.example.androidbase.features.auth.ui.register
sealed interface RegisterUiIntent {
    data class NameChanged(val value: String) : RegisterUiIntent
    data class EmailChanged(val value: String) : RegisterUiIntent
    data class PasswordChanged(val value: String) : RegisterUiIntent
    data class TermsChanged(val value: Boolean) : RegisterUiIntent
    data object TogglePassword : RegisterUiIntent
    data object Submit : RegisterUiIntent
}
