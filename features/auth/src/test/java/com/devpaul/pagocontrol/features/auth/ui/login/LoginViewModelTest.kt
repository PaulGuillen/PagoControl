package com.devpaul.pagocontrol.features.auth.ui.login

import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun validationPreventsRepositoryCall() = runTest {
        val repository = ControlledAuth()
        val viewModel = LoginViewModel(repository, SavedStateHandle())
        viewModel.onIntent(LoginUiIntent.Submit)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.emailInvalid)
        assertTrue(viewModel.state.value.passwordInvalid)
        assertEquals(0, repository.calls)
    }

    @Test fun duplicateSubmitIsIgnoredAndFailureCanRetry() = runTest {
        val repository = ControlledAuth()
        val handle = SavedStateHandle()
        val viewModel = LoginViewModel(repository, handle)
        viewModel.onIntent(LoginUiIntent.FillDemo)
        viewModel.onIntent(LoginUiIntent.Submit)
        viewModel.onIntent(LoginUiIntent.Submit)
        runCurrent()
        assertEquals(1, repository.calls)
        assertTrue(viewModel.state.value.loading)
        repository.response.complete(SignInResult.InvalidCredentials)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.loading)
        assertEquals(LoginError.InvalidCredentials, viewModel.state.value.error)
        repository.response = CompletableDeferred(SignInResult.Success)
        viewModel.onIntent(LoginUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(2, repository.calls)
        assertEquals("", viewModel.state.value.password)
        assertFalse(handle.contains("password"))
    }

    private class ControlledAuth : AuthRepository {
        override val session = MutableStateFlow<UserSession?>(null)
        var calls = 0
        var response = CompletableDeferred<SignInResult>()
        override suspend fun signIn(email: String, password: String): SignInResult {
            calls++
            return response.await()
        }
        override suspend fun signOut() { session.value = null }
    }
}
