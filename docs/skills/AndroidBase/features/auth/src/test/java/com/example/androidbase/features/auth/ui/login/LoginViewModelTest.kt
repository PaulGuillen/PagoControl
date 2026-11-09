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
}
