package com.example.androidbase.features.onboarding.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.androidbase.domain.StartupPreferences
import com.example.androidbase.platform.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class OnboardingViewModel(
    private val preferences: StartupPreferences,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<OnboardingUiState>(OnboardingUiState(page = (savedStateHandle.get<Int>("page") ?: 0).coerceIn(0, 2))) {
    fun onIntent(intent: OnboardingUiIntent) {
        if (state.value.saving || state.value.destination != null) return
        when (intent) {
            OnboardingUiIntent.Next -> {
                if (state.value.page == 2) finish(OnboardingDestination.Login)
                else selectPage(state.value.page + 1)
            }
            OnboardingUiIntent.Back -> selectPage((state.value.page - 1).coerceAtLeast(0))
            is OnboardingUiIntent.Finish -> finish(intent.destination)
        }
    }
    private fun selectPage(page: Int) {
        savedStateHandle["page"] = page
        updateState { it.copy(page = page, failed = false) }
    }
    private fun finish(destination: OnboardingDestination) {
        updateState { it.copy(saving = true, failed = false) }
        viewModelScope.launch {
            try {
                preferences.completeOnboarding()
                updateState { it.copy(saving = false, destination = destination) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(saving = false, failed = true) }
            }
        }
    }
}
