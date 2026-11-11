package com.devpaul.features.auth.ui.forgot

sealed interface ForgotPasswordUiIntent {
    data class Send(val email: String) : ForgotPasswordUiIntent
    data object ClearError : ForgotPasswordUiIntent
}
