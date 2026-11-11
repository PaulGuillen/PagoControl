package com.devpaul.features.auth.domain.usecase

import com.devpaul.features.auth.domain.entity.AuthUser
import com.devpaul.features.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class GetCurrentUserUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): AuthUser? = repository.currentUser()
}
