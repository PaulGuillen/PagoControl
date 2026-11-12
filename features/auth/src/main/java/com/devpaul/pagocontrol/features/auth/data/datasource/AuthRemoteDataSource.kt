package com.devpaul.pagocontrol.features.auth.data.datasource

import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRemoteDataSource {
    val session: StateFlow<UserSession?>

    suspend fun refreshProfile(): UserSession? = session.value

    suspend fun signIn(email: String, password: String)

    suspend fun register(name: String, email: String, password: String)

    suspend fun sendPasswordReset(email: String)

    suspend fun updateName(name: String)

    suspend fun signOut()
}
