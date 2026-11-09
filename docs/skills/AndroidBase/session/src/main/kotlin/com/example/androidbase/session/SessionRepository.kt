package com.example.androidbase.session

import com.example.androidbase.domain.AppResult
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val state: StateFlow<SessionState>
    val isDemo: Boolean
    suspend fun signIn(email: String, password: String): AppResult<Unit>
    suspend fun register(email: String, password: String): AppResult<Unit>
    suspend fun resetPassword(email: String): AppResult<Unit>
    fun signOut()
}
