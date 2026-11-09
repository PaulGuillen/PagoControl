package com.devpaul.pagocontrol.session

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val isDemo: Boolean get() = false
    val session: StateFlow<UserSession?>
    suspend fun signIn(email: String, password: String): SignInResult
    suspend fun signOut()
}
