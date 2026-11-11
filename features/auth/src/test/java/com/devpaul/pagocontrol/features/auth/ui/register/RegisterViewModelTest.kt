package com.devpaul.pagocontrol.features.auth.ui.register

import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.features.auth.TestAuthRepository
import com.devpaul.pagocontrol.features.auth.domain.entity.AuthUser
import com.devpaul.pagocontrol.features.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }
    @Test fun validatesConsentAndIgnoresDuplicateSubmit() = runTest {
        val repository = ControlledAuth()
        val handle = SavedStateHandle()
        val viewModel = RegisterViewModel(RegisterUseCase(repository), handle)
        viewModel.onIntent(RegisterUiIntent.Submit)
        assertEquals(RegisterError.InvalidInput, viewModel.state.value.error)
        viewModel.onIntent(RegisterUiIntent.NameChanged("Paul"))
        viewModel.onIntent(RegisterUiIntent.EmailChanged("paul@example.com"))
        viewModel.onIntent(RegisterUiIntent.PasswordChanged("Password123"))
        viewModel.onIntent(RegisterUiIntent.Submit)
        assertEquals(RegisterError.TermsRequired, viewModel.state.value.error)
        assertEquals(0, repository.calls)
        viewModel.onIntent(RegisterUiIntent.TermsChanged(true))
        viewModel.onIntent(RegisterUiIntent.Submit)
        viewModel.onIntent(RegisterUiIntent.Submit)
        runCurrent()
        assertEquals(1, repository.calls)
        repository.response.complete(repository.user)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.completed)
        assertEquals("", viewModel.state.value.password)
        assertFalse(handle.contains("password"))
    }
    private class ControlledAuth : TestAuthRepository() {
        var calls = 0
        val response = CompletableDeferred<AuthUser>()
        override suspend fun register(name: String, email: String, password: String): AuthUser { calls++; return response.await() }
    }
}
