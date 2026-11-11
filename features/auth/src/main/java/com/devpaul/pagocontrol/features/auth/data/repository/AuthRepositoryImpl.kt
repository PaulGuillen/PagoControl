package com.devpaul.pagocontrol.features.auth.data.repository

import com.devpaul.pagocontrol.features.auth.data.datasource.AuthRemoteDataSource
import com.devpaul.pagocontrol.features.auth.data.datasource.remote.ProfileSetupException
import com.devpaul.pagocontrol.features.auth.data.security.AuthAction
import com.devpaul.pagocontrol.features.auth.data.security.AuthAttemptLimiter
import com.devpaul.pagocontrol.features.auth.domain.entity.AuthUser
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthException
import com.devpaul.pagocontrol.features.auth.domain.repository.AuthRepository
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import com.devpaul.pagocontrol.session.UserSession
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
class AuthRepositoryImpl(
    private val source: AuthRemoteDataSource,
    private val attemptLimiter: AuthAttemptLimiter
) : AuthRepository {
    override fun currentUser(): AuthUser? = source.session.value?.toDomain()
    override suspend fun currentUserProfile(): AuthUser? =
        firebaseCall { source.refreshProfile()?.toDomain() }

    override suspend fun signIn(email: String, password: String): AuthUser {
        attemptLimiter.recordAttempt(AuthAction.Login)
        return firebaseCall { source.signIn(email, password); checkNotNull(currentUser()) }
    }

    override suspend fun register(name: String, email: String, password: String): AuthUser {
        attemptLimiter.recordAttempt(AuthAction.Register)
        return firebaseCall { source.register(name, email, password); checkNotNull(currentUser()) }
    }

    override suspend fun sendPasswordReset(email: String) {
        attemptLimiter.recordAttempt(AuthAction.PasswordReset)
        firebaseCall {
            try {
                source.sendPasswordReset(email)
            } catch (error: FirebaseAuthException) {
                if (error.errorCode != "ERROR_USER_NOT_FOUND") throw error
            }
        }
    }

    override suspend fun updateUserName(name: String): AuthUser = firebaseCall {
        source.updateName(name)
        checkNotNull(currentUser())
    }

    override suspend fun signOut() = source.signOut()

    private suspend fun <T> firebaseCall(operation: suspend () -> T): T = try {
        operation()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        AppAnalytics.error("auth_request", error)
        throw AuthException(
            when (error) {
                is AuthException -> error.error
                is ProfileSetupException -> AuthError.PROFILE_INCOMPLETE
                is FirebaseNetworkException -> AuthError.NETWORK
                is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
                is FirebaseAuthException -> when (error.errorCode) {
                    "ERROR_INVALID_EMAIL" -> AuthError.INVALID_EMAIL
                    "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EMAIL_ALREADY_IN_USE
                    "ERROR_WEAK_PASSWORD" -> AuthError.SHORT_PASSWORD
                    "ERROR_USER_DISABLED" -> AuthError.USER_DISABLED
                    "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS" -> AuthError.INVALID_CREDENTIALS
                    "ERROR_TOO_MANY_REQUESTS" -> AuthError.TOO_MANY_REQUESTS
                    else -> AuthError.UNKNOWN
                }

                else -> AuthError.UNKNOWN
            }, error
        )
    }

    private fun UserSession.toDomain() = AuthUser(id, name, email)
}
