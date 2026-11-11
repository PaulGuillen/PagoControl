package com.devpaul.features.auth.ui.session

import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.features.auth.domain.entity.AuthUser
import com.devpaul.features.auth.domain.repository.AuthRepository
import com.devpaul.features.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.devpaul.features.auth.domain.usecase.GetCurrentUserUseCase
import com.devpaul.features.auth.domain.usecase.SignOutUseCase
import com.devpaul.features.auth.domain.usecase.UpdateUserNameUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthSessionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `profile stays loading until Firebase name arrives`() = runTest(dispatcher) {
        val gate = CompletableDeferred<AuthUser?>()
        repository.profileGate = gate
        val viewModel = viewModel()
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals("", viewModel.uiState.value.name)
        assertEquals("", viewModel.uiState.value.initials)

        gate.complete(AuthUser("user-id", "  Elena   Vargas  ", "elena@example.com"))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Elena   Vargas", viewModel.uiState.value.name)
        assertEquals("EV", viewModel.uiState.value.initials)
        assertEquals("elena@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `profile failure ends loading and uses Firebase Auth fallback`() = runTest(dispatcher) {
        repository.failProfile = true
        repository.current = AuthUser("user-id", "Ana Torres", "ana@example.com")
        val viewModel = viewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Ana Torres", viewModel.uiState.value.name)
        assertEquals("AT", viewModel.uiState.value.initials)
    }

    @Test
    fun `missing profile ends loading without invented initials`() = runTest(dispatcher) {
        repository.current = null
        val viewModel = viewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("", viewModel.uiState.value.name)
        assertEquals("", viewModel.uiState.value.initials)
    }

    @Test
    fun `logout cancels pending profile and clears identity`() = runTest(dispatcher) {
        val gate = CompletableDeferred<AuthUser?>()
        repository.profileGate = gate
        val viewModel = viewModel()
        runCurrent()
        viewModel.logout()
        gate.complete(AuthUser("old-user", "Old Account", "old@example.com"))
        advanceUntilIdle()
        assertEquals(UserProfileUiState(isLoading = false), viewModel.uiState.value)
    }

    @Test
    fun `editing the name updates both greeting and initials`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(null, viewModel.updateUserName("Maria Lopez"))
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Maria Lopez", viewModel.uiState.value.name)
        assertEquals("ML", viewModel.uiState.value.initials)
    }

    @Test
    fun `initials handle whitespace single names and accented names`() {
        assertEquals("PG", UserProfileUiState(name = " Paul\tGuillen Acuña ").initials)
        assertEquals("É", UserProfileUiState(name = "élena").initials)
        assertEquals("", UserProfileUiState(name = " \n ").initials)
    }

    private fun viewModel() = AuthSessionViewModel(
        GetCurrentUserUseCase(repository),
        GetCurrentUserProfileUseCase(repository),
        SignOutUseCase(repository),
        UpdateUserNameUseCase(repository),
    )

    private class FakeAuthRepository : AuthRepository {
        var current: AuthUser? = AuthUser("user-id", "Paul Guillen", "paul@example.com")
        var profileGate: CompletableDeferred<AuthUser?>? = null
        var failProfile = false

        override fun currentUser() = current

        override suspend fun currentUserProfile(): AuthUser? {
            if (failProfile) error("Profile unavailable")
            return profileGate?.await() ?: current
        }

        override suspend fun updateUserName(name: String): AuthUser = checkNotNull(current).copy(name = name).also {
            current = it
        }

        override fun signOut() {
            current = null
        }

        override suspend fun signIn(email: String, password: String): AuthUser = error("Not used")
        override suspend fun register(name: String, email: String, password: String): AuthUser = error("Not used")
        override suspend fun sendPasswordReset(email: String) = Unit
    }
}
