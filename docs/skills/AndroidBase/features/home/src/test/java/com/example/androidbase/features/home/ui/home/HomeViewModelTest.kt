package com.example.androidbase.features.home.ui.home

import com.example.androidbase.domain.AddExampleUseCase
import com.example.androidbase.domain.AppPreferences
import com.example.androidbase.domain.AppResult
import com.example.androidbase.domain.ExampleItem
import com.example.androidbase.domain.ExampleRepository
import com.example.androidbase.platform.EventReporter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() { Dispatchers.setMain(dispatcher) }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun savesOnlyOnceDuringAnActiveRequestAndUsesSessionOwner() = runTest(dispatcher) {
        var requests = 0
        var savedOwner: String? = null
        val response = CompletableDeferred<AppResult<Unit>>()
        val repository = object : ExampleRepository {
            override fun observe(ownerId: String) = flowOf(emptyList<ExampleItem>())
            override suspend fun add(ownerId: String, title: String): AppResult<Unit> {
                requests++
                savedOwner = ownerId
                return response.await()
            }
        }
        val preferences = object : AppPreferences {
            override val darkTheme = flowOf(false)
            override suspend fun setDarkTheme(enabled: Boolean) = Unit
        }
        val reporter = object : EventReporter {
            override fun screen(name: String) = Unit
            override fun action(name: String) = Unit
            override fun error(operation: String) = Unit
        }
        val viewModel = HomeViewModel(
            "current-owner",
            repository,
            AddExampleUseCase(repository),
            preferences,
            reporter
        )
        runCurrent()
        assertFalse(viewModel.state.value.isLoading)
        viewModel.accept(HomeUiIntent.Add("First"))
        viewModel.accept(HomeUiIntent.Add("Duplicate"))
        runCurrent()
        assertTrue(viewModel.state.value.isSaving)
        assertEquals(1, requests)
        assertEquals("current-owner", savedOwner)
        response.complete(AppResult.Success(Unit))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSaving)
        assertEquals(1, viewModel.state.value.savedCount)
    }
}
