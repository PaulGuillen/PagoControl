package com.example.androidbase.data

import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.example.androidbase.session.AccountRepository
import com.example.androidbase.session.AccountResult
import com.example.androidbase.session.SessionRepository

class SessionAccountRepository(private val session: SessionRepository) : AccountRepository {
    override suspend fun register(name: String, email: String, password: String): AccountResult =
        if (session.isDemo) AccountResult.DemoOnly else mapResult(session.register(email, password))

    override suspend fun sendPasswordReset(email: String): AccountResult =
        if (session.isDemo) AccountResult.DemoOnly else mapResult(session.resetPassword(email))

    private fun mapResult(result: AppResult<Unit>): AccountResult = when (result) {
        is AppResult.Success -> AccountResult.Success
        is AppResult.Failure -> when (result.reason) {
            AppFailure.INVALID_INPUT, AppFailure.INVALID_CREDENTIALS -> AccountResult.InvalidInput
            AppFailure.NETWORK -> AccountResult.Network
            AppFailure.UNAVAILABLE, AppFailure.UNKNOWN -> AccountResult.Unavailable
        }
    }
}
