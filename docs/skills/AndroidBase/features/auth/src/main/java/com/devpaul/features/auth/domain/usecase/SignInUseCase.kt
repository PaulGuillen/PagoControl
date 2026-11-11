package com.devpaul.features.auth.domain.usecase

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.core.domain.result.useCaseResult
import com.devpaul.features.auth.domain.entity.AuthUser
import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.exception.AuthException
import com.devpaul.features.auth.domain.exception.AuthFailure
import com.devpaul.features.auth.domain.mapper.toAuthFailure
import com.devpaul.features.auth.domain.repository.AuthRepository
import com.devpaul.features.auth.domain.validation.validateEmail
import org.koin.core.annotation.Factory

@Factory
class SignInUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): UseCaseResult<AuthUser, AuthFailure> = useCaseResult(
        errorMapper = Exception::toAuthFailure,
    ) {
        validateEmail(email)
        if (password.isBlank()) throw AuthException(AuthError.EMPTY_PASSWORD)
        repository.signIn(email.trim(), password)
    }
}
