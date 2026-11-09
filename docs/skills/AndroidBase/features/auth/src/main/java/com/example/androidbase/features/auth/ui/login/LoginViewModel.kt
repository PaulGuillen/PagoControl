package com.example.androidbase.features.auth.ui.login

import androidx.lifecycle.SavedStateHandle
import com.example.androidbase.platform.BaseViewModel
import com.example.androidbase.platform.EventReporter
import androidx.lifecycle.viewModelScope
import com.example.androidbase.session.SessionRepository
import com.example.androidbase.domain.LoginValidation
import com.example.androidbase.domain.AppResult
import com.example.androidbase.domain.AppFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val repository: SessionRepository,
    private val savedStateHandle: SavedStateHandle,
    private val reporter: EventReporter,
) : BaseViewModel<LoginUiState>(LoginUiState(email = savedStateHandle["email"] ?: "")) {

    fun onIntent(intent: LoginUiIntent) {
        if (state.value.loading) return
        when (intent) {
            is LoginUiIntent.EmailChanged -> {
                savedStateHandle["email"] = intent.value
                updateState { it.copy(email = intent.value, emailInvalid = false, error = null) }
            }
            is LoginUiIntent.PasswordChanged -> updateState {
                it.copy(password = intent.value, passwordInvalid = false, error = null)
            }
            LoginUiIntent.TogglePassword -> updateState { it.copy(passwordVisible = !it.passwordVisible) }
            LoginUiIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = state.value
        val emailInvalid = !repository.isDemo && !LoginValidation.validEmail(current.email)
        val passwordInvalid = !repository.isDemo && current.password.length < 8
        if (emailInvalid || passwordInvalid) {
            updateState { it.copy(emailInvalid = emailInvalid, passwordInvalid = passwordInvalid) }
            return
        }
        updateState { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = try {
                repository.signIn(current.email.trim(), current.password)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                AppResult.Failure(AppFailure.UNAVAILABLE)
            }
            if (result is AppResult.Success) reporter.action("auth_success") else reporter.error("auth_request")
            updateState {
                it.copy(
                    loading = false,
                    password = if (result is AppResult.Success) "" else it.password,
                    error = when (result) {
                        is AppResult.Success -> null
                        is AppResult.Failure -> when (result.reason) {
                            AppFailure.INVALID_CREDENTIALS, AppFailure.INVALID_INPUT -> LoginError.InvalidCredentials
                            AppFailure.NETWORK -> LoginError.Network
                            else -> LoginError.Unavailable
                        }
                    },
                )
            }
        }
    }
}
