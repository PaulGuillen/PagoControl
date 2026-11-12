package com.devpaul.pagocontrol.features.auth.ui.forgotpassword

import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.features.auth.TestAuthRepository
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthException
import com.devpaul.pagocontrol.features.auth.domain.usecase.SendPasswordResetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }

    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun validatesEmailAndAllowsRetryAfterNetworkFailure() = runTest {
        val repository = ControlledAuth()
        val viewModel = ForgotPasswordViewModel(SendPasswordResetUseCase(repository), SavedStateHandle())
        viewModel.onIntent(ForgotPasswordUiIntent.Submit)
        assertEquals(ForgotPasswordError.InvalidEmail, viewModel.state.value.error)
        assertEquals(0, repository.calls)
        viewModel.onIntent(ForgotPasswordUiIntent.EmailChanged("paul@example.com"))
        viewModel.onIntent(ForgotPasswordUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(ForgotPasswordError.Network, viewModel.state.value.error)
        repository.fail = false
        viewModel.onIntent(ForgotPasswordUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(2, repository.calls)
        assertTrue(viewModel.state.value.completed)
    }
    private class ControlledAuth : TestAuthRepository() {
        var calls = 0
        var fail = true

        override suspend fun sendPasswordReset(email: String) { calls++; if (fail) throw AuthException(AuthError.NETWORK) }
    }
}
