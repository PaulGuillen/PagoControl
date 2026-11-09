package com.example.androidbase.domain
import kotlinx.coroutines.flow.Flow

interface StartupPreferences {
    val onboardingCompleted: Flow<Boolean>
    suspend fun completeOnboarding()
}
