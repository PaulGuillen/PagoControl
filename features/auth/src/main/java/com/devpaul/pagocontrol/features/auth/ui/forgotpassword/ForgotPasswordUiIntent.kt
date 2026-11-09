package com.devpaul.pagocontrol.features.auth.ui.forgotpassword
sealed interface ForgotPasswordUiIntent {
    data class EmailChanged(val value: String) : ForgotPasswordUiIntent
    data object Submit : ForgotPasswordUiIntent
}
