package com.example.androidbase.session

interface AccountRepository {
    suspend fun register(name: String, email: String, password: String): AccountResult
    suspend fun sendPasswordReset(email: String): AccountResult
}
