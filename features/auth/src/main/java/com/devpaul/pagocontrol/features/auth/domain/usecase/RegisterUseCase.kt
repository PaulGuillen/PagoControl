package com.devpaul.pagocontrol.features.auth.domain.usecase

import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.domain.result.useCaseResult
import com.devpaul.pagocontrol.features.auth.domain.entity.AuthUser
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthException
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthFailure
import com.devpaul.pagocontrol.features.auth.domain.mapper.toAuthFailure
import com.devpaul.pagocontrol.features.auth.domain.repository.AuthRepository
import com.devpaul.pagocontrol.features.auth.domain.validation.MIN_PASSWORD_LENGTH
import com.devpaul.pagocontrol.features.auth.domain.validation.validateEmail
import org.koin.core.annotation.Factory

@Factory
class RegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
    ): UseCaseResult<AuthUser, AuthFailure> = useCaseResult(
        errorMapper = Exception::toAuthFailure,
    ) {
        if (name.trim().length !in 2..80) throw AuthException(AuthError.INVALID_NAME)
        validateEmail(email)
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw AuthException(AuthError.SHORT_PASSWORD)
        }
        repository.register(name.trim(), email.trim(), password)
    }
}
