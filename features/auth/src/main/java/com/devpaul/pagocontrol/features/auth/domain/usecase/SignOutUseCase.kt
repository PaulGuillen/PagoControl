package com.devpaul.pagocontrol.features.auth.domain.usecase

import com.devpaul.pagocontrol.features.auth.domain.repository.AuthRepository
import org.koin.core.annotation.Factory

@Factory
class SignOutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.signOut()
}
