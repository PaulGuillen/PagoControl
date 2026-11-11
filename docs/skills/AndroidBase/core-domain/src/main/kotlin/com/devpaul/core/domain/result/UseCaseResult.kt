package com.devpaul.core.domain.result

import kotlinx.coroutines.CancellationException

sealed interface UseCaseResult<out T, out E> {
    data class Success<T>(val value: T) : UseCaseResult<T, Nothing>
    data class Error<E>(val error: E) : UseCaseResult<Nothing, E>
}

suspend inline fun <T, E> useCaseResult(
    crossinline errorMapper: (Exception) -> E,
    operation: suspend () -> T,
): UseCaseResult<T, E> = try {
    UseCaseResult.Success(operation())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    UseCaseResult.Error(errorMapper(error))
}
