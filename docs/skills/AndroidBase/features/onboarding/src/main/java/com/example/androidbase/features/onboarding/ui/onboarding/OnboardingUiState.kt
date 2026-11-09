package com.example.androidbase.features.onboarding.ui.onboarding
data class OnboardingUiState(
    val page: Int = 0,
    val saving: Boolean = false,
    val failed: Boolean = false,
    val destination: OnboardingDestination? = null,
)
