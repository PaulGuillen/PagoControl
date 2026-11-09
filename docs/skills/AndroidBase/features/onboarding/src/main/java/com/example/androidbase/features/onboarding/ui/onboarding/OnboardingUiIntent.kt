package com.example.androidbase.features.onboarding.ui.onboarding
sealed interface OnboardingUiIntent {
    data object Next : OnboardingUiIntent
    data object Back : OnboardingUiIntent
    data class Finish(val destination: OnboardingDestination) : OnboardingUiIntent
}
