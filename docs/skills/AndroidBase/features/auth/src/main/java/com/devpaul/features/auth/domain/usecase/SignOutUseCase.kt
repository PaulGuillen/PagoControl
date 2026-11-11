package com.devpaul.features.auth.domain.usecase

import com.devpaul.features.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class SignOutUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.signOut()
}
