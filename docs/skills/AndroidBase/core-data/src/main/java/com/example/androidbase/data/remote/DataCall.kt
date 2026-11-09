package com.example.androidbase.data.remote

import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.AppResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.CancellationException
import java.io.IOException

internal suspend fun <Value> dataCall(operation: suspend () -> Value): AppResult<Value> = try {
    AppResult.Success(operation())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    AppResult.Failure(
        when (error) {
            is FirebaseNetworkException, is IOException -> AppFailure.NETWORK
            is FirebaseAuthInvalidCredentialsException -> AppFailure.INVALID_CREDENTIALS
            else -> AppFailure.UNKNOWN
        },
    )
}
