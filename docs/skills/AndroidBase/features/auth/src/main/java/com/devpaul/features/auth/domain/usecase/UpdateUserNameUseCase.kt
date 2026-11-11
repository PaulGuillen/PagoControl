package com.devpaul.features.auth.domain.usecase

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.core.domain.result.useCaseResult
import com.devpaul.features.auth.domain.entity.AuthUser
import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.exception.AuthException
import com.devpaul.features.auth.domain.exception.AuthFailure
import com.devpaul.features.auth.domain.mapper.toAuthFailure
import com.devpaul.features.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class UpdateUserNameUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(name: String): UseCaseResult<AuthUser, AuthFailure> = useCaseResult(
        errorMapper = Exception::toAuthFailure,
    ) {
        val normalizedName = name.trim()
        if (normalizedName.length < 2) throw AuthException(AuthError.INVALID_NAME)
        repository.updateUserName(normalizedName)
    }
}
