package com.devpaul.pagocontrol.features.auth

import com.devpaul.pagocontrol.features.auth.domain.entity.AuthUser
import com.devpaul.pagocontrol.features.auth.domain.repository.AuthRepository

open class TestAuthRepository : AuthRepository {
    val user = AuthUser("uid", "Paul", "paul@example.com")

    override fun currentUser(): AuthUser? = null

    override suspend fun currentUserProfile(): AuthUser? = currentUser()

    override suspend fun signIn(email: String, password: String): AuthUser = user

    override suspend fun register(name: String, email: String, password: String): AuthUser = user

    override suspend fun sendPasswordReset(email: String) {}

    override suspend fun updateUserName(name: String): AuthUser = user.copy(name = name)

    override suspend fun signOut() {}
}
