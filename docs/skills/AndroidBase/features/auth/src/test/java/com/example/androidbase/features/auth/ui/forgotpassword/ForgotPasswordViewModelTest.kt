package com.example.androidbase.features.auth.ui.forgotpassword

import androidx.lifecycle.SavedStateHandle
import com.example.androidbase.session.AccountRepository
import com.example.androidbase.session.AccountResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun validatesEmailAndAllowsRetryWithoutClaimingEmailWasSentInDemo() = runTest {
        val repository = ControlledAccount()
        val viewModel = ForgotPasswordViewModel(repository, SavedStateHandle())
        viewModel.onIntent(ForgotPasswordUiIntent.Submit)
        assertEquals(ForgotPasswordError.InvalidEmail, viewModel.state.value.error)
        assertEquals(0, repository.calls)
        viewModel.onIntent(ForgotPasswordUiIntent.EmailChanged("demo@test.pe"))
        viewModel.onIntent(ForgotPasswordUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(ForgotPasswordError.Network, viewModel.state.value.error)
        repository.result = AccountResult.DemoOnly
        viewModel.onIntent(ForgotPasswordUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(2, repository.calls)
        assertTrue(viewModel.state.value.completed)
        assertTrue(viewModel.state.value.demoOnly)
    }

    private class ControlledAccount : AccountRepository {
        var calls = 0
        var result = AccountResult.Network
        override suspend fun register(name: String, email: String, password: String) = AccountResult.Unavailable
        override suspend fun sendPasswordReset(email: String): AccountResult {
            calls++
            return result
        }
    }
}
