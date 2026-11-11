package com.devpaul.features.auth.domain.repository

import com.devpaul.features.auth.domain.entity.AuthUser

interface AuthRepository {
    fun currentUser(): AuthUser?

    suspend fun currentUserProfile(): AuthUser?

    suspend fun signIn(email: String, password: String): AuthUser

    suspend fun register(name: String, email: String, password: String): AuthUser

    suspend fun sendPasswordReset(email: String)

    suspend fun updateUserName(name: String): AuthUser

    fun signOut()
}
