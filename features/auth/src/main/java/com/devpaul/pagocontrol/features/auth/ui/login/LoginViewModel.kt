package com.devpaul.pagocontrol.features.auth.ui.login

import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.platform.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.LoginValidation
import com.devpaul.pagocontrol.session.SignInResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val repository: AuthRepository,
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
        }
    }

    private fun submit() {
        val current = state.value
        val emailInvalid = !repository.isDemo && !LoginValidation.validEmail(current.email)
        val passwordInvalid = !repository.isDemo && !LoginValidation.validPassword(current.password)
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
                    },
                )
            }
        }
    }
}
