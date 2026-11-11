package com.devpaul.features.auth.domain.validation

import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.exception.AuthException

internal const val MIN_PASSWORD_LENGTH = 8
private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

internal fun validateEmail(email: String) {
    if (!emailRegex.matches(email.trim())) {
        throw AuthException(AuthError.INVALID_EMAIL)
    }
}
