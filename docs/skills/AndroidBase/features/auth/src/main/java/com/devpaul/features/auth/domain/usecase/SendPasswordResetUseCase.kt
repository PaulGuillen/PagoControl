package com.devpaul.features.auth.domain.usecase

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.core.domain.result.useCaseResult
import com.devpaul.features.auth.domain.exception.AuthFailure
import com.devpaul.features.auth.domain.mapper.toAuthFailure
import com.devpaul.features.auth.domain.repository.AuthRepository
import com.devpaul.features.auth.domain.validation.validateEmail
import org.koin.core.annotation.Factory

@Factory
class SendPasswordResetUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String): UseCaseResult<Unit, AuthFailure> = useCaseResult(
        errorMapper = Exception::toAuthFailure,
    ) {
        validateEmail(email)
        repository.sendPasswordReset(email.trim())
    }
}
