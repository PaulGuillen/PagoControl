package com.devpaul.pagocontrol.session

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<UserSession?>
    suspend fun signIn(email: String, password: String): SignInResult
    suspend fun signOut()
}
