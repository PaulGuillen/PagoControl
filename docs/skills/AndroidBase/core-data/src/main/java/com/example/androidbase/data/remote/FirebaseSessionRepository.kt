package com.example.androidbase.data.remote

import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.example.androidbase.session.SessionRepository
import com.example.androidbase.session.SessionState
import com.example.androidbase.session.SessionUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseSessionRepository(private val auth: FirebaseAuth) : SessionRepository {
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val state = mutableState.asStateFlow()
    override val isDemo = false
    private val listener = FirebaseAuth.AuthStateListener { provider ->
        mutableState.value = provider.currentUser?.let {
            SessionState.SignedIn(SessionUser(id = it.uid, email = it.email, isDemo = false))
        } ?: SessionState.SignedOut
    }

    init {
        auth.addAuthStateListener(listener)
    }

    override suspend fun signIn(email: String, password: String): AppResult<Unit> {
        if (email.isBlank() || password.isEmpty()) return AppResult.Failure(AppFailure.INVALID_INPUT)
        return dataCall {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Unit
        }
    }

    override suspend fun register(email: String, password: String): AppResult<Unit> {
        if (email.isBlank() || password.length < 6) return AppResult.Failure(AppFailure.INVALID_INPUT)
        return dataCall {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
            Unit
        }
    }

    override suspend fun resetPassword(email: String): AppResult<Unit> {
        if (email.isBlank()) return AppResult.Failure(AppFailure.INVALID_INPUT)
        return dataCall {
            auth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }
    }

    override fun signOut() = auth.signOut()

    fun close() {
        auth.removeAuthStateListener(listener)
    }
}
