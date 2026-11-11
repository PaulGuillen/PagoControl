package com.devpaul.features.auth.data.security

import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.exception.AuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

enum class AuthAction {
    Login,
    Register,
    PasswordReset,
}

interface AuthAttemptStore {
    fun read(action: AuthAction): List<Long>
    fun write(action: AuthAction, attempts: List<Long>)
}

@Single
class AuthAttemptLimiter(private val store: AuthAttemptStore) {
    suspend fun recordAttempt(action: AuthAction) = withContext(Dispatchers.IO) {
        recordAttempt(action, System.currentTimeMillis())
    }

    @Synchronized
    internal fun recordAttempt(action: AuthAction, nowMillis: Long) {
        val activeAttempts = store.read(action).filter { timestamp ->
            nowMillis < timestamp || nowMillis - timestamp < WINDOW_MILLIS
        }
        if (activeAttempts.size >= MAX_ATTEMPTS) {
            throw AuthException(AuthError.ATTEMPT_LIMIT_REACHED)
        }
        store.write(action, activeAttempts + nowMillis)
    }

    companion object {
        const val MAX_ATTEMPTS = 3
        const val WINDOW_MILLIS = 15 * 60 * 1000L
    }
}
