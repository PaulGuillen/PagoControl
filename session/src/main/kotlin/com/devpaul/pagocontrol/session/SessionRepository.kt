package com.devpaul.pagocontrol.session

import kotlinx.coroutines.flow.StateFlow

data class UserSession(val id: String, val name: String, val email: String)

sealed interface SignInResult {
    data object Success : SignInResult
    data object InvalidCredentials : SignInResult
    data object Unavailable : SignInResult
}

interface AuthRepository {
    val session: StateFlow<UserSession?>
    suspend fun signIn(email: String, password: String): SignInResult
    suspend fun signOut()
}
