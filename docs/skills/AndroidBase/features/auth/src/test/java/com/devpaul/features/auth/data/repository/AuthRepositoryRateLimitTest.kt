package com.devpaul.features.auth.data.repository

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.features.auth.data.datasource.remote.FirebaseAuthDataSource
import com.devpaul.features.auth.data.security.AuthAction
import com.devpaul.features.auth.data.security.AuthAttemptLimiter
import com.devpaul.features.auth.data.security.AuthAttemptStore
import com.devpaul.features.auth.data.security.MemoryAuthAttemptStore
import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.exception.AuthException
import com.devpaul.features.auth.domain.usecase.SignInUseCase
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class AuthRepositoryRateLimitTest {
    private val dataSource = mock(FirebaseAuthDataSource::class.java)
    private val store = MemoryAuthAttemptStore()
    private val limiter = AuthAttemptLimiter(store)
    private val repository = AuthRepositoryImpl(dataSource, limiter)

    @Test
    fun `fourth failed login never reaches Firebase even with a different email`() = runTest {
        val invalidCredentials = mock(FirebaseAuthException::class.java)
        `when`(invalidCredentials.errorCode).thenReturn("ERROR_INVALID_CREDENTIAL")
        `when`(dataSource.signIn("user@example.com", "password123"))
            .thenAnswer { throw invalidCredentials }
        repeat(3) {
            assertEquals(AuthError.INVALID_CREDENTIALS, failure { repository.signIn("user@example.com", "password123") }.error)
        }
        assertEquals(
            AuthError.ATTEMPT_LIMIT_REACHED,
            failure { repository.signIn("other@example.com", "password123") }.error,
        )
        verify(dataSource, times(3)).signIn("user@example.com", "password123")
    }

    @Test
    fun `blocked registration and password recovery never reach Firebase`() = runTest {
        repeat(3) {
            limiter.recordAttempt(AuthAction.Register)
            limiter.recordAttempt(AuthAction.PasswordReset)
        }
        assertEquals(
            AuthError.ATTEMPT_LIMIT_REACHED,
            failure { repository.register("User", "user@example.com", "password123") }.error,
        )
        assertEquals(
            AuthError.ATTEMPT_LIMIT_REACHED,
            failure { repository.sendPasswordReset("user@example.com") }.error,
        )
        verifyNoInteractions(dataSource)
    }

    @Test
    fun `invalid input consumes no attempts and makes no Firebase request`() = runTest {
        val result = SignInUseCase(repository)("invalid", "password123")
        assertTrue(result is UseCaseResult.Error)
        assertEquals(emptyList<Long>(), store.read(AuthAction.Login))
        verifyNoInteractions(dataSource)
    }

    @Test
    fun `successful password reset requests also count toward the limit`() = runTest {
        repeat(3) { repository.sendPasswordReset("user@example.com") }
        assertEquals(
            AuthError.ATTEMPT_LIMIT_REACHED,
            failure { repository.sendPasswordReset("user@example.com") }.error,
        )
        verify(dataSource, times(3)).sendPasswordReset("user@example.com")
    }

    @Test
    fun `Firebase throttling uses the existing too many requests error`() = runTest {
        val throttled = mock(FirebaseTooManyRequestsException::class.java)
        `when`(dataSource.signIn("user@example.com", "password123"))
            .thenAnswer { throw throttled }
        assertEquals(
            AuthError.TOO_MANY_REQUESTS,
            failure { repository.signIn("user@example.com", "password123") }.error,
        )
    }

    @Test
    fun `storage failure prevents an uncounted Firebase request`() = runTest {
        val failingStore = object : AuthAttemptStore {
            override fun read(action: AuthAction) = emptyList<Long>()
            override fun write(action: AuthAction, attempts: List<Long>) = error("Storage unavailable")
        }
        val repository = AuthRepositoryImpl(dataSource, AuthAttemptLimiter(failingStore))
        val result = SignInUseCase(repository)("user@example.com", "password123")
        assertTrue(result is UseCaseResult.Error)
        verifyNoInteractions(dataSource)
    }

    private suspend fun failure(operation: suspend () -> Unit): AuthException {
        try {
            operation()
        } catch (error: AuthException) {
            return error
        }
        error("Expected an authentication error")
    }
}
