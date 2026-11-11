package com.devpaul.features.auth.domain.exception

data class AuthFailure(
    val error: AuthError,
    val type: AuthFailureType,
)
