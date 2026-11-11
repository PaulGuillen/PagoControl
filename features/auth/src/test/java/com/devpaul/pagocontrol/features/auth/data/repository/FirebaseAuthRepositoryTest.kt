package com.devpaul.pagocontrol.features.auth.data.repository

import com.devpaul.pagocontrol.features.auth.data.datasource.AuthRemoteDataSource
import com.devpaul.pagocontrol.features.auth.data.security.AuthAction
import com.devpaul.pagocontrol.features.auth.data.security.AuthAttemptLimiter
import com.devpaul.pagocontrol.features.auth.data.security.AuthAttemptStore
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthException
import com.devpaul.pagocontrol.features.auth.domain.usecase.SignInUseCase
import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FirebaseAuthRepositoryTest {
    @Test fun repositoryAndUseCaseKeepValidationSeparateFromNetworkCalls() = runTest {
        val source = FakeSource()
        val repository = AuthRepositoryImpl(source, AuthAttemptLimiter(MemoryAttempts()))
        val result = SignInUseCase(repository)("", "")
        assertTrue(result is UseCaseResult.Error)
        assertEquals(0, source.calls)
        assertTrue(SignInUseCase(repository)(" paul@example.com ", " password ") is UseCaseResult.Success)
        assertEquals("paul@example.com", source.lastEmail)
        assertEquals(" password ", source.lastPassword)
        repository.signOut()
        assertNull(repository.currentUser())
    }
    @Test fun preservesTypedFailuresAndCancellation() = runTest {
        val source = FakeSource()
        val repository = AuthRepositoryImpl(source, AuthAttemptLimiter(MemoryAttempts()))
        source.failure = AuthException(AuthError.NETWORK)
        val result = SignInUseCase(repository)("paul@example.com", "password")
        assertEquals(AuthError.NETWORK, (result as UseCaseResult.Error).error.error)
        source.failure = CancellationException("Cancelled")
        var cancelled = false
        try { repository.signIn("paul@example.com", "password") } catch (_: CancellationException) { cancelled = true }
        assertTrue(cancelled)
    }
    @Test fun attemptLimitPersistsAcrossLimiterInstances() {
        val store = MemoryAttempts()
        val first = AuthAttemptLimiter(store)
        repeat(3) { first.recordAttempt(AuthAction.Login, 1_000L + it) }
        val recreated = AuthAttemptLimiter(store)
        try { recreated.recordAttempt(AuthAction.Login, 2_000L); fail("Expected throttling") }
        catch (error: AuthException) { assertEquals(AuthError.ATTEMPT_LIMIT_REACHED, error.error) }
        recreated.recordAttempt(AuthAction.PasswordReset, 2_000L)
        recreated.recordAttempt(AuthAction.Login, AuthAttemptLimiter.WINDOW_MILLIS + 2_000L)
    }
    private class MemoryAttempts : AuthAttemptStore {
        private val values = mutableMapOf<AuthAction, List<Long>>()
        override fun read(action: AuthAction) = values[action].orEmpty()
        override fun write(action: AuthAction, attempts: List<Long>) { values[action] = attempts }
    }
    private class FakeSource : AuthRemoteDataSource {
        override val session = MutableStateFlow<UserSession?>(null)
        var failure: Exception? = null
        var calls = 0
        var lastEmail = ""
        var lastPassword = ""
        override suspend fun signIn(email: String, password: String) {
            calls++
            failure?.let { throw it }
            lastEmail = email
            lastPassword = password
            session.value = UserSession("uid", "Paul", email)
        }
        override suspend fun register(name: String, email: String, password: String) {}
        override suspend fun sendPasswordReset(email: String) {}
        override suspend fun updateName(name: String) {}
        override suspend fun signOut() { session.value = null }
    }
}
