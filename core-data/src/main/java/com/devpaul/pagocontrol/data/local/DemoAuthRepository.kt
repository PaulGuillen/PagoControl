package com.devpaul.pagocontrol.data.local

import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoAuthRepository : AuthRepository {
    private val mutableSession = MutableStateFlow<UserSession?>(null)
    override val session = mutableSession.asStateFlow()

    override suspend fun signIn(email: String, password: String): SignInResult {
        delay(350)
        if (!email.trim().equals("demo@pagocontrol.pe", ignoreCase = true) || password != "PagoControl123") {
            return SignInResult.InvalidCredentials
        }
        mutableSession.value = UserSession(id = "demo", name = "Carlos", email = "demo@pagocontrol.pe")
        return SignInResult.Success
    }

    override suspend fun signOut() {
        mutableSession.value = null
    }
}
