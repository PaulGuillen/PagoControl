package com.devpaul.pagocontrol.domain
import kotlinx.coroutines.flow.Flow

interface StartupPreferences {
    val onboardingCompleted: Flow<Boolean>
    suspend fun completeOnboarding()
}
