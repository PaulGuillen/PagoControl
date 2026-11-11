package com.devpaul.features.auth.domain.usecase

import com.devpaul.features.auth.domain.entity.AuthUser
import com.devpaul.features.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class GetCurrentUserProfileUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthUser? = repository.currentUserProfile()
}
