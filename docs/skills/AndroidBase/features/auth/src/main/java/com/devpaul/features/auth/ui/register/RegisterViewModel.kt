package com.devpaul.features.auth.ui.register

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.lifecycle.StatefulViewModel
import com.devpaul.features.auth.domain.exception.AuthFailure
import com.devpaul.features.auth.domain.exception.AuthFailureType
import com.devpaul.features.auth.domain.usecase.RegisterUseCase
import com.devpaul.features.auth.ui.mapper.toMessageResource
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
) : StatefulViewModel<RegisterUiState, RegisterUiIntent, RegisterUiEvent>(RegisterUiState()) {

    override suspend fun onUiIntent(intent: RegisterUiIntent) {
        when (intent) {
            is RegisterUiIntent.Register -> register(intent)
            RegisterUiIntent.ClearError -> clearError()
        }
    }

    private suspend fun register(intent: RegisterUiIntent.Register) {
        if (uiState.value.isLoading) return

        updateUiState { it.copy(isLoading = true, errorMessage = null) }
        when (val result = registerUseCase(intent.name, intent.email, intent.password)) {
            is UseCaseResult.Success -> {
                AppAnalytics.outcome("register", "success")
                updateUiState { RegisterUiState() }
                emitUiEvent(RegisterUiEvent.Authenticated)
            }

            is UseCaseResult.Error -> handleError(result.error)
        }
    }

    private suspend fun handleError(failure: AuthFailure) {
        AppAnalytics.error("register", code = failure.error.name.lowercase())
        val message = failure.error.toMessageResource()
        updateUiState {
            it.copy(
                isLoading = false,
                errorMessage = message.takeIf { failure.type == AuthFailureType.VALIDATION },
            )
        }
        if (failure.type == AuthFailureType.OPERATION) {
            emitUiEvent(RegisterUiEvent.ShowSnackbar(message))
        }
    }

    private fun clearError() {
        updateUiState { it.copy(errorMessage = null) }
    }
}
