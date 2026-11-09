package com.example.androidbase.features.auth.ui.login

import androidx.lifecycle.viewModelScope
import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.example.androidbase.platform.BaseViewModel
import com.example.androidbase.platform.EventReporter
import com.example.androidbase.session.SessionRepository
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val session: SessionRepository,
    private val reporter: EventReporter,
) : BaseViewModel<LoginUiState>(LoginUiState()) {
    val isDemo = session.isDemo

    fun accept(intent: LoginUiIntent) {
        if (state.value.isLoading) return
        updateState { LoginUiState(isLoading = true) }
        viewModelScope.launch {
            val result = when (intent) {
                is LoginUiIntent.SignIn -> session.signIn(intent.email, intent.password)
                is LoginUiIntent.Register -> session.register(intent.email, intent.password)
                is LoginUiIntent.ResetPassword -> session.resetPassword(intent.email)
            }
            when (result) {
                is AppResult.Success -> {
                    reporter.action("auth_success")
                    updateState { LoginUiState(resetSent = intent is LoginUiIntent.ResetPassword) }
                }
                is AppResult.Failure -> {
                    reporter.error("auth_request")
                    updateState {
                        LoginUiState(failure = when (result.reason) {
                            AppFailure.INVALID_INPUT -> LoginError.InvalidInput
                            AppFailure.INVALID_CREDENTIALS -> LoginError.InvalidCredentials
                            AppFailure.NETWORK -> LoginError.Network
                            AppFailure.UNAVAILABLE, AppFailure.UNKNOWN -> LoginError.Unavailable
                        })
                    }
                }
            }
        }
    }
}
