package com.devpaul.pagocontrol.features.auth.domain

import com.devpaul.pagocontrol.domain.result.UseCaseResult
import com.devpaul.pagocontrol.features.auth.TestAuthRepository
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.features.auth.domain.exception.AuthFailureType
import com.devpaul.pagocontrol.features.auth.domain.usecase.RegisterUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.SendPasswordResetUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.UpdateUserNameUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {
    @Test fun invalidRegistrationIsClassifiedAsValidation() = runTest {
        val result = RegisterUseCase(TestAuthRepository())("P", "paul@example.com", "Password123")
        assertTrue(result is UseCaseResult.Error)
        val failure = (result as UseCaseResult.Error).error
        assertEquals(AuthError.INVALID_NAME, failure.error)
        assertEquals(AuthFailureType.VALIDATION, failure.type)
    }

    @Test fun rejectsInvalidEmailAndOverlongProfileNames() = runTest {
        val reset = SendPasswordResetUseCase(TestAuthRepository())("a@.")
        assertEquals(AuthError.INVALID_EMAIL, (reset as UseCaseResult.Error).error.error)
        val update = UpdateUserNameUseCase(TestAuthRepository())("a".repeat(81))
        assertEquals(AuthError.INVALID_NAME, (update as UseCaseResult.Error).error.error)
    }
}
