package com.devpaul.features.auth.domain.usecase

import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.features.auth.domain.entity.AuthUser
import com.devpaul.features.auth.domain.exception.AuthError
import com.devpaul.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUserNameUseCaseTest {

    @Test
    fun `trims and updates a valid name`() = runBlocking {
        val repository = FakeAuthRepository()
        val result = UpdateUserNameUseCase(repository)("  Elena Vargas  ")

        assertTrue(result is UseCaseResult.Success)
        assertEquals("Elena Vargas", repository.updatedName)
    }

    @Test
    fun `rejects a name shorter than two characters`() = runBlocking {
        val result = UpdateUserNameUseCase(FakeAuthRepository())(" E ")

        assertTrue(result is UseCaseResult.Error)
        assertEquals(AuthError.INVALID_NAME, (result as UseCaseResult.Error).error.error)
    }
}

private class FakeAuthRepository : AuthRepository {
    var updatedName: String? = null

    override fun currentUser(): AuthUser? = null
    override suspend fun currentUserProfile(): AuthUser? = null
    override suspend fun signIn(email: String, password: String) = error("Not used")
    override suspend fun register(name: String, email: String, password: String) = error("Not used")
    override suspend fun sendPasswordReset(email: String) = Unit
    override fun signOut() = Unit

    override suspend fun updateUserName(name: String): AuthUser {
        updatedName = name
        return AuthUser("user-id", name, "elena@example.com")
    }
}
