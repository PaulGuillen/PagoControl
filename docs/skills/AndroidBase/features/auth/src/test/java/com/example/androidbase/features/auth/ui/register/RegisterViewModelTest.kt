package com.example.androidbase.features.auth.ui.register

import androidx.lifecycle.SavedStateHandle
import com.example.androidbase.session.AccountRepository
import com.example.androidbase.session.AccountResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun validatesConsentAndIgnoresDoubleSubmit() = runTest {
        val repository = ControlledAccount()
        val saved = SavedStateHandle()
        val viewModel = RegisterViewModel(repository, saved)
        viewModel.onIntent(RegisterUiIntent.Submit)
        assertEquals(RegisterError.InvalidInput, viewModel.state.value.error)
        viewModel.onIntent(RegisterUiIntent.NameChanged("Paul"))
        viewModel.onIntent(RegisterUiIntent.EmailChanged("demo@test.pe"))
        viewModel.onIntent(RegisterUiIntent.PasswordChanged("Password123"))
        viewModel.onIntent(RegisterUiIntent.Submit)
        assertEquals(RegisterError.TermsRequired, viewModel.state.value.error)
        assertEquals(0, repository.calls)
        viewModel.onIntent(RegisterUiIntent.TermsChanged(true))
        viewModel.onIntent(RegisterUiIntent.Submit)
        viewModel.onIntent(RegisterUiIntent.Submit)
        runCurrent()
        assertEquals(1, repository.calls)
        repository.response.complete(AccountResult.DemoOnly)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.completed)
        assertTrue(viewModel.state.value.demoOnly)
        assertEquals("", viewModel.state.value.password)
        assertFalse(saved.contains("password"))
    }

    private class ControlledAccount : AccountRepository {
        var calls = 0
        val response = CompletableDeferred<AccountResult>()
        override suspend fun register(name: String, email: String, password: String): AccountResult {
            calls++
            return response.await()
        }
        override suspend fun sendPasswordReset(email: String) = AccountResult.Unavailable
    }
}
