package com.example.androidbase.domain

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddExampleUseCaseTest {
    private class FakeRepository : ExampleRepository {
        var savedTitle: String? = null
        override fun observe(ownerId: String) = flowOf(emptyList<ExampleItem>())
        override suspend fun add(ownerId: String, title: String): AppResult<Unit> {
            savedTitle = title
            return AppResult.Success(Unit)
        }
    }

    @Test
    fun trimsInputBeforeSaving() = runTest {
        val repository = FakeRepository()
        assertEquals(AppResult.Success(Unit), AddExampleUseCase(repository)("owner", "  First  "))
        assertEquals("First", repository.savedTitle)
    }

    @Test
    fun rejectsBlankOwnerAndInvalidTitles() = runTest {
        val repository = FakeRepository()
        val add = AddExampleUseCase(repository)
        for ((owner, title) in listOf("" to "Title", "owner" to "  ", "owner" to "x".repeat(121))) {
            assertEquals(AppResult.Failure(AppFailure.INVALID_INPUT), add(owner, title))
        }
        assertNull(repository.savedTitle)
    }

    @Test
    fun acceptsMaximumLength() = runTest {
        val repository = FakeRepository()
        assertEquals(AppResult.Success(Unit), AddExampleUseCase(repository)("owner", "x".repeat(120)))
    }
}
