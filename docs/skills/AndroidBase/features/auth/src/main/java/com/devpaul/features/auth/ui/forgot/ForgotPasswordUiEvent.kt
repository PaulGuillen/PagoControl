package com.devpaul.features.auth.ui.forgot

sealed interface ForgotPasswordUiEvent {
    data class PasswordResetSent(val email: String) : ForgotPasswordUiEvent
    data class ShowSnackbar(val message: Int) : ForgotPasswordUiEvent
}
