package com.devpaul.pagocontrol.features.auth.domain.validation

import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthException

internal const val MIN_PASSWORD_LENGTH = 8

internal fun validateEmail(email: String) {
    if (!com.devpaul.pagocontrol.domain.LoginValidation.validEmail(email)) {
        throw AuthException(AuthError.INVALID_EMAIL)
    }
}
