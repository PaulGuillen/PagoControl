package com.devpaul.features.auth.ui.forgot

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.lifecycle.StatefulViewModel
import com.devpaul.features.auth.domain.exception.AuthFailure
import com.devpaul.features.auth.domain.exception.AuthFailureType
import com.devpaul.features.auth.domain.usecase.SendPasswordResetUseCase
import com.devpaul.features.auth.ui.mapper.toMessageResource
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ForgotPasswordViewModel(
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
) : StatefulViewModel<ForgotPasswordUiState, ForgotPasswordUiIntent, ForgotPasswordUiEvent>(
    ForgotPasswordUiState(),
) {

    override suspend fun onUiIntent(intent: ForgotPasswordUiIntent) {
        when (intent) {
            is ForgotPasswordUiIntent.Send -> sendPasswordReset(intent.email)
            ForgotPasswordUiIntent.ClearError -> clearError()
        }
    }

    private suspend fun sendPasswordReset(email: String) {
        if (uiState.value.isLoading) return

        updateUiState { it.copy(isLoading = true, errorMessage = null) }
        when (val result = sendPasswordResetUseCase(email)) {
            is UseCaseResult.Success -> {
                AppAnalytics.outcome("password_reset", "success")
                updateUiState { ForgotPasswordUiState() }
                emitUiEvent(ForgotPasswordUiEvent.PasswordResetSent(email))
            }

            is UseCaseResult.Error -> handleError(result.error)
        }
    }

    private suspend fun handleError(failure: AuthFailure) {
        AppAnalytics.error("password_reset", code = failure.error.name.lowercase())
        val message = failure.error.toMessageResource()
        updateUiState {
            it.copy(
                isLoading = false,
                errorMessage = message.takeIf { failure.type == AuthFailureType.VALIDATION },
            )
        }
        if (failure.type == AuthFailureType.OPERATION) {
            emitUiEvent(ForgotPasswordUiEvent.ShowSnackbar(message))
        }
    }

    private fun clearError() {
        updateUiState { it.copy(errorMessage = null) }
    }
}
