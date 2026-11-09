package com.devpaul.pagocontrol.features.auth.ui.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailInvalid: Boolean = false,
    val passwordInvalid: Boolean = false,
    val loading: Boolean = false,
    val error: LoginError? = null,
)
