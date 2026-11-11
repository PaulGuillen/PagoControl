package com.devpaul.pagocontrol.features.auth.ui.login

import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.features.auth.domain.mapper.toSignInResult
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.platform.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.features.auth.domain.usecase.SignInUseCase
import com.devpaul.pagocontrol.domain.LoginValidation
import com.devpaul.pagocontrol.session.SignInResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val signIn: SignInUseCase,
    private val savedStateHandle: SavedStateHandle,
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
            LoginUiIntent.ClearError -> updateState { it.copy(error = null, emailInvalid = false, passwordInvalid = false) }
        }
    }

    private fun submit() {
        val current = state.value
        val emailInvalid = !LoginValidation.validEmail(current.email)
        val passwordInvalid = current.password.isBlank()
        if (emailInvalid || passwordInvalid) {
            updateState { it.copy(emailInvalid = emailInvalid, passwordInvalid = passwordInvalid) }
            return
        }
        updateState { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = try {
                when (val response = signIn(current.email, current.password)) {
                    is UseCaseResult.Success -> {
                        AppAnalytics.outcome("login", "success")
                        SignInResult.Success
                    }
                    is UseCaseResult.Error -> {
                        AppAnalytics.error("login", code = response.error.error.name.lowercase())
                        response.error.error.toSignInResult()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                SignInResult.Unavailable
            }
            updateState {
                it.copy(
                    loading = false,
                    password = if (result == SignInResult.Success) "" else it.password,
                    error = when (result) {
                        SignInResult.Success -> null
                        SignInResult.InvalidCredentials -> LoginError.InvalidCredentials
                        SignInResult.Unavailable -> LoginError.Unavailable
                        SignInResult.Network -> LoginError.Network
                        SignInResult.TooManyRequests -> LoginError.TooManyRequests
                        SignInResult.UserDisabled -> LoginError.UserDisabled
                        SignInResult.ProfileIncomplete -> LoginError.ProfileIncomplete
                    },
                )
            }
        }
    }
}
