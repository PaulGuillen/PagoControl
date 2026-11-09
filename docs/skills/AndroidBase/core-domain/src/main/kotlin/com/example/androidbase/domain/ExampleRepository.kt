package com.example.androidbase.domain

import kotlinx.coroutines.flow.Flow

data class ExampleItem(val id: String, val title: String)

interface ExampleRepository {
    fun observe(ownerId: String): Flow<List<ExampleItem>>
    suspend fun add(ownerId: String, title: String): AppResult<Unit>
}

class AddExampleUseCase(private val repository: ExampleRepository) {
    suspend operator fun invoke(ownerId: String, title: String): AppResult<Unit> {
        val normalized = title.trim()
        if (ownerId.isBlank() || normalized.isEmpty() || normalized.length > 120) {
            return AppResult.Failure(AppFailure.INVALID_INPUT)
        }
        return repository.add(ownerId, normalized)
    }
}
