package com.example.androidbase.features.auth.ui.login

import androidx.lifecycle.SavedStateHandle

import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.example.androidbase.platform.EventReporter
import com.example.androidbase.session.SessionRepository
import com.example.androidbase.session.SessionState
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

    @Before
    fun setup() { Dispatchers.setMain(dispatcher) }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun ignoresSecondSubmitAndExposesFailure() = runTest(dispatcher) {
        val response = CompletableDeferred<AppResult<Unit>>()
        var requests = 0
        val repository = object : SessionRepository {
            override val state = MutableStateFlow<SessionState>(SessionState.SignedOut)
            override val isDemo = false
            override suspend fun signIn(email: String, password: String): AppResult<Unit> {
                requests++
                return response.await()
            }
            override suspend fun register(email: String, password: String) = signIn(email, password)
            override suspend fun resetPassword(email: String) = AppResult.Success(Unit)
            override fun signOut() = Unit
        }
        val reporter = object : EventReporter {
            override fun screen(name: String) = Unit
            override fun action(name: String) = Unit
            override fun error(operation: String) = Unit
        }
        val viewModel = LoginViewModel(repository = repository, savedStateHandle = SavedStateHandle(), reporter = reporter)
        viewModel.onIntent(LoginUiIntent.EmailChanged("demo@test.pe"))
        viewModel.onIntent(LoginUiIntent.PasswordChanged("password"))
        viewModel.onIntent(LoginUiIntent.Submit)
        viewModel.onIntent(LoginUiIntent.Submit)
        runCurrent()
        assertTrue(viewModel.state.value.loading)
        assertEquals(1, requests)
        response.complete(AppResult.Failure(AppFailure.NETWORK))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.loading)
        assertEquals(LoginError.Network, viewModel.state.value.error)
    }
    @Test fun localProviderAcceptsEmptyFields() = runTest(dispatcher) {
        val repository = ImmediateSession(isDemo = true)
        val viewModel = LoginViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(),
            reporter = SilentReporter,
        )
        viewModel.onIntent(LoginUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(1, repository.requests)
        assertFalse(viewModel.state.value.emailInvalid)
        assertFalse(viewModel.state.value.passwordInvalid)
    }

    @Test fun realProviderStillRejectsEmptyFields() = runTest(dispatcher) {
        val repository = ImmediateSession(isDemo = false)
        val viewModel = LoginViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(),
            reporter = SilentReporter,
        )
        viewModel.onIntent(LoginUiIntent.Submit)
        advanceUntilIdle()
        assertEquals(0, repository.requests)
        assertTrue(viewModel.state.value.emailInvalid)
        assertTrue(viewModel.state.value.passwordInvalid)
    }

    private class ImmediateSession(override val isDemo: Boolean) : SessionRepository {
        override val state = MutableStateFlow<SessionState>(SessionState.SignedOut)
        var requests = 0
        override suspend fun signIn(email: String, password: String): AppResult<Unit> {
            requests++
            return AppResult.Success(Unit)
        }
        override suspend fun register(email: String, password: String) = AppResult.Failure(AppFailure.UNAVAILABLE)
        override suspend fun resetPassword(email: String) = AppResult.Failure(AppFailure.UNAVAILABLE)
        override fun signOut() { state.value = SessionState.SignedOut }
    }

    private object SilentReporter : EventReporter {
        override fun screen(name: String) = Unit
        override fun action(name: String) = Unit
        override fun error(operation: String) = Unit
    }
}
