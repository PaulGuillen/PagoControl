package com.devpaul.features.auth.domain.exception

class AuthException(
    val error: AuthError,
    cause: Throwable? = null,
) : Exception(error.name, cause)
