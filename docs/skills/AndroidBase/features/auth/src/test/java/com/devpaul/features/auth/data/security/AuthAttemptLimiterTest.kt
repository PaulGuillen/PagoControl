package com.devpaul.features.auth.data.security

import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.exception.AuthException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AuthAttemptLimiterTest {
    private val store = MemoryAuthAttemptStore()
    private val limiter = AuthAttemptLimiter(store)

    @Test
    fun `three attempts are allowed and the fourth is blocked`() {
        repeat(3) { limiter.recordAttempt(AuthAction.Login, 1000) }

        val error = assertThrows(AuthException::class.java) {
            limiter.recordAttempt(AuthAction.Login, 1000)
        }
        assertEquals(AuthError.ATTEMPT_LIMIT_REACHED, error.error)
        assertEquals(3, store.read(AuthAction.Login).size)
    }

    @Test
    fun `each attempt expires exactly fifteen minutes after its reservation`() {
        limiter.recordAttempt(AuthAction.Login, 0)
        limiter.recordAttempt(AuthAction.Login, 1)
        limiter.recordAttempt(AuthAction.Login, 2)
        assertThrows(AuthException::class.java) {
            limiter.recordAttempt(AuthAction.Login, AuthAttemptLimiter.WINDOW_MILLIS - 1)
        }

        limiter.recordAttempt(AuthAction.Login, AuthAttemptLimiter.WINDOW_MILLIS)
        assertEquals(listOf(1L, 2L, AuthAttemptLimiter.WINDOW_MILLIS), store.read(AuthAction.Login))
        assertThrows(AuthException::class.java) {
            limiter.recordAttempt(AuthAction.Login, AuthAttemptLimiter.WINDOW_MILLIS)
        }
    }

    @Test
    fun `login registration and password recovery have independent limits`() {
        AuthAction.entries.forEach { action ->
            repeat(3) { limiter.recordAttempt(action, 1000) }
            assertThrows(AuthException::class.java) { limiter.recordAttempt(action, 1000) }
        }
    }

    @Test
    fun `recreating the limiter does not reset stored attempts`() {
        repeat(3) { limiter.recordAttempt(AuthAction.PasswordReset, 1000) }
        val recreatedLimiter = AuthAttemptLimiter(store)

        assertThrows(AuthException::class.java) {
            recreatedLimiter.recordAttempt(AuthAction.PasswordReset, 1000)
        }
    }

    @Test
    fun `blocked requests do not extend the cooldown`() {
        repeat(3) { limiter.recordAttempt(AuthAction.Register, 1000) }
        val previous = store.read(AuthAction.Register)
        assertThrows(AuthException::class.java) { limiter.recordAttempt(AuthAction.Register, 5000) }
        assertEquals(previous, store.read(AuthAction.Register))
    }

    @Test
    fun `moving the clock backward does not clear attempts`() {
        repeat(3) { limiter.recordAttempt(AuthAction.Login, 1000) }
        assertThrows(AuthException::class.java) { limiter.recordAttempt(AuthAction.Login, 500) }
    }

    @Test
    fun `concurrent reservations cannot exceed three attempts`() {
        val executor = Executors.newFixedThreadPool(8)
        try {
            val requests = List(30) {
                Callable {
                    try {
                        limiter.recordAttempt(AuthAction.Login, 1000)
                        true
                    } catch (_: AuthException) {
                        false
                    }
                }
            }
            val results = executor.invokeAll(requests).map { it.get() }
            assertEquals(3, results.count { it })
            assertEquals(3, store.read(AuthAction.Login).size)
        } finally {
            executor.shutdownNow()
        }
    }
}

internal class MemoryAuthAttemptStore : AuthAttemptStore {
    private val attempts = mutableMapOf<AuthAction, List<Long>>()

    override fun read(action: AuthAction): List<Long> = attempts[action].orEmpty()

    override fun write(action: AuthAction, attempts: List<Long>) {
        this.attempts[action] = attempts
    }
}
