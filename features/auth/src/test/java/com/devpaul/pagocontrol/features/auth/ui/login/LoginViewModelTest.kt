package com.devpaul.pagocontrol.features.auth.ui.login

import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.features.auth.TestAuthRepository
import com.devpaul.pagocontrol.features.auth.domain.entity.AuthUser
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthException
import com.devpaul.pagocontrol.features.auth.domain.usecase.SignInUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }
    @Test fun emptyFieldsNeverAuthenticate() = runTest {
        val repository = ControlledAuth()
        val viewModel = LoginViewModel(SignInUseCase(repository), SavedStateHandle())
        viewModel.onIntent(LoginUiIntent.Submit)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.emailInvalid)
        assertTrue(viewModel.state.value.passwordInvalid)
        assertEquals(0, repository.calls)
    }
    @Test fun duplicateSubmitIsIgnoredAndFailureCanRetryWithoutPersistingPassword() = runTest {
        val repository = ControlledAuth()
        val handle = SavedStateHandle()
        val viewModel = LoginViewModel(SignInUseCase(repository), handle)
        viewModel.onIntent(LoginUiIntent.EmailChanged("paul@example.com"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("example-password"))
        viewModel.onIntent(LoginUiIntent.Submit)
        viewModel.onIntent(LoginUiIntent.Submit)
        runCurrent()
        assertEquals(1, repository.calls)
        repository.response.completeExceptionally(AuthException(AuthError.INVALID_CREDENTIALS))
        advanceUntilIdle()
        assertEquals(LoginError.InvalidCredentials, viewModel.state.value.error)
        assertFalse(viewModel.state.value.loading)
        repository.response = CompletableDeferred(repository.user)
        viewModel.onIntent(LoginUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(2, repository.calls)
        assertEquals("", viewModel.state.value.password)
        assertFalse(handle.contains("password"))
    }
    private class ControlledAuth : TestAuthRepository() {
        var calls = 0
        var response = CompletableDeferred<AuthUser>()
        override suspend fun signIn(email: String, password: String): AuthUser { calls++; return response.await() }
    }
}
