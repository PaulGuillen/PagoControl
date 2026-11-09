package com.devpaul.pagocontrol.features.splash.ui.splash

import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.domain.StartupPreferences
import com.devpaul.pagocontrol.platform.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SplashViewModel(private val preferences: StartupPreferences) : BaseViewModel<SplashUiState>(SplashUiState()) {
    private var loading = false
    init { load() }
    fun onIntent(intent: SplashUiIntent) { if (intent == SplashUiIntent.Retry) load() }
    private fun load() {
        if (loading) return
        loading = true
        updateState { SplashUiState() }
        viewModelScope.launch {
            try {
                val completed = preferences.onboardingCompleted.first()
                delay(650)
                updateState { SplashUiState(onboardingCompleted = completed) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { SplashUiState(failed = true) }
            } finally {
                loading = false
            }
        }
    }
}
