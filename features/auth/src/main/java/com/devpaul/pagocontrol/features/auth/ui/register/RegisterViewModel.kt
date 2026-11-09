package com.devpaul.pagocontrol.features.auth.ui.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.platform.BaseViewModel
import com.devpaul.pagocontrol.session.AccountRepository
import com.devpaul.pagocontrol.session.AccountResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RegisterViewModel(
    private val repository: AccountRepository,
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
        }
    }
    private fun submit() {
        val current = state.value
        val validEmail = current.email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
        val error = when {
            current.name.trim().length < 2 || !validEmail || current.password.length < 8 -> RegisterError.InvalidInput
            !current.termsAccepted -> RegisterError.TermsRequired
            else -> null
        }
        updateState { it.copy(validationAttempted = true, error = error) }
        if (error != null) return
        updateState { it.copy(loading = true) }
        viewModelScope.launch {
            val result = try {
                repository.register(name = current.name.trim(), email = current.email.trim(), password = current.password)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                AccountResult.Unavailable
            }
            updateState {
                it.copy(
                    loading = false,
                    completed = result == AccountResult.Success || result == AccountResult.DemoOnly,
                    demoOnly = result == AccountResult.DemoOnly,
                    password = if (result == AccountResult.Success || result == AccountResult.DemoOnly) "" else it.password,
                    error = when (result) {
                        AccountResult.InvalidInput -> RegisterError.InvalidInput
                        AccountResult.Network -> RegisterError.Network
                        AccountResult.Unavailable -> RegisterError.Unavailable
                        else -> null
                    },
                )
            }
        }
    }
}
