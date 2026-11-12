package com.devpaul.pagocontrol.features.auth.ui.register

import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.features.auth.domain.mapper.toAccountResult
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.platform.BaseViewModel
import com.devpaul.pagocontrol.features.auth.domain.usecase.RegisterUseCase
import com.devpaul.pagocontrol.session.AccountResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RegisterViewModel(
    private val register: RegisterUseCase,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<RegisterUiState>(RegisterUiState(
    name = savedStateHandle["name"] ?: "", email = savedStateHandle["email"] ?: "",
)) {
    fun onIntent(intent: RegisterUiIntent) {
        if (state.value.loading || state.value.completed) return
        when (intent) {
            is RegisterUiIntent.NameChanged -> {
                savedStateHandle["name"] = intent.value
                updateState { it.copy(name = intent.value, error = null) }
            }
            is RegisterUiIntent.EmailChanged -> {
                savedStateHandle["email"] = intent.value
                updateState { it.copy(email = intent.value, error = null) }
            }
            is RegisterUiIntent.PasswordChanged -> updateState { it.copy(password = intent.value, error = null) }
            is RegisterUiIntent.TermsChanged -> updateState { it.copy(termsAccepted = intent.value, error = null) }
            RegisterUiIntent.TogglePassword -> updateState { it.copy(passwordVisible = !it.passwordVisible) }
            RegisterUiIntent.Submit -> submit()
            RegisterUiIntent.ClearError -> updateState { it.copy(error = null, validationAttempted = false) }
        }
    }

    private fun submit() {
        val current = state.value
        val validEmail = current.email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        val error = when {
            current.name.trim().length !in 2..80 || !validEmail || current.password.length < 8 -> RegisterError.InvalidInput
            !current.termsAccepted -> RegisterError.TermsRequired
            else -> null
        }
        updateState { it.copy(validationAttempted = true, error = error) }
        if (error != null) return
        updateState { it.copy(loading = true) }
        viewModelScope.launch {
            val result = try {
                when (val response = register(name = current.name, email = current.email, password = current.password)) {
                    is UseCaseResult.Success -> {
                        AppAnalytics.outcome("register", "success")
                        AccountResult.Success
                    }
                    is UseCaseResult.Error -> {
                        AppAnalytics.error("register", code = response.error.error.name.lowercase())
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
                    password = if (result == AccountResult.Success) "" else it.password,
                    error = when (result) {
                        AccountResult.InvalidInput -> RegisterError.InvalidInput
                        AccountResult.Network -> RegisterError.Network
                        AccountResult.Unavailable -> RegisterError.Unavailable
                        AccountResult.EmailAlreadyInUse -> RegisterError.EmailAlreadyInUse
                        AccountResult.WeakPassword -> RegisterError.InvalidInput
                        AccountResult.TooManyRequests -> RegisterError.TooManyRequests
                        AccountResult.ProfileIncomplete -> RegisterError.ProfileIncomplete
                        else -> null
                    },
                )
            }
        }
    }
}
