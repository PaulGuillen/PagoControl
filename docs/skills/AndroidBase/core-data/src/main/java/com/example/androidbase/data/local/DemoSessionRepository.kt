package com.example.androidbase.data.local

import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.example.androidbase.session.SessionRepository
import com.example.androidbase.session.SessionState
import com.example.androidbase.session.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoSessionRepository : SessionRepository {
    private val mutableState = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val state = mutableState.asStateFlow()
    override val isDemo = true

    override suspend fun signIn(email: String, password: String): AppResult<Unit> {
        mutableState.value = SessionState.SignedIn(SessionUser(id = "demo", email = null, isDemo = true))
        return AppResult.Success(Unit)
    }

    override suspend fun register(email: String, password: String): AppResult<Unit> =
        AppResult.Failure(AppFailure.UNAVAILABLE)

    override suspend fun resetPassword(email: String): AppResult<Unit> =
        AppResult.Failure(AppFailure.UNAVAILABLE)

    override fun signOut() {
        mutableState.value = SessionState.SignedOut
    }
}
