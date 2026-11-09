package com.example.androidbase.session

import com.example.androidbase.domain.AppResult
import kotlinx.coroutines.flow.StateFlow

data class SessionUser(val id: String, val email: String?, val isDemo: Boolean)

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: SessionUser) : SessionState
}

interface SessionRepository {
    val state: StateFlow<SessionState>
    val isDemo: Boolean
    suspend fun signIn(email: String, password: String): AppResult<Unit>
    suspend fun register(email: String, password: String): AppResult<Unit>
    suspend fun resetPassword(email: String): AppResult<Unit>
    fun signOut()
}
