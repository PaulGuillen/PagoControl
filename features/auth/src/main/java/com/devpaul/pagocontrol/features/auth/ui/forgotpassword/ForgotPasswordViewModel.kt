package com.devpaul.pagocontrol.features.auth.ui.forgotpassword

import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.features.auth.domain.mapper.toAccountResult
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.platform.BaseViewModel
import com.devpaul.pagocontrol.features.auth.domain.usecase.SendPasswordResetUseCase
import com.devpaul.pagocontrol.session.AccountResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ForgotPasswordViewModel(
    private val sendPasswordReset: SendPasswordResetUseCase,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<ForgotPasswordUiState>(ForgotPasswordUiState(email = savedStateHandle["email"] ?: "")) {
    fun onIntent(intent: ForgotPasswordUiIntent) {
        if (state.value.loading || state.value.completed) return
        when (intent) {
            is ForgotPasswordUiIntent.EmailChanged -> {
                savedStateHandle["email"] = intent.value
                updateState { it.copy(email = intent.value, error = null) }
            }
            ForgotPasswordUiIntent.Submit -> submit()
            ForgotPasswordUiIntent.ClearError -> updateState { it.copy(error = null) }
        }
    }

    private fun submit() {
        val email = state.value.email.trim()
        if (!email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            updateState { it.copy(error = ForgotPasswordError.InvalidEmail) }
            return
        }
        updateState { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = try {
                when (val response = sendPasswordReset(email)) {
                    is UseCaseResult.Success -> {
                        AppAnalytics.outcome("password_reset", "success")
                        AccountResult.Success
                    }
                    is UseCaseResult.Error -> {
                        AppAnalytics.error("password_reset", code = response.error.error.name.lowercase())
                        response.error.error.toAccountResult()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                AccountResult.Unavailable
            }
            updateState {
                it.copy(
                    loading = false,
                    completed = result == AccountResult.Success,
                    error = when (result) {
                        AccountResult.InvalidInput -> ForgotPasswordError.InvalidEmail
                        AccountResult.Network -> ForgotPasswordError.Network
                        AccountResult.Unavailable -> ForgotPasswordError.Unavailable
                        AccountResult.TooManyRequests -> ForgotPasswordError.TooManyRequests
                        else -> null
                    },
                )
            }
        }
    }
}
