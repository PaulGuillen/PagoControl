package com.devpaul.pagocontrol.features.onboarding.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import com.devpaul.pagocontrol.domain.StartupPreferences
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }

    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun keepsPageAndRetriesPreferenceWrite() = runTest {
        val preferences = ControlledPreferences()
        val saved = SavedStateHandle()
        val viewModel = OnboardingViewModel(preferences, saved)
        viewModel.onIntent(OnboardingUiIntent.Next)
        viewModel.onIntent(OnboardingUiIntent.Next)
        assertEquals(2, saved.get<Int>("page"))
        viewModel.onIntent(OnboardingUiIntent.Next)
        viewModel.onIntent(OnboardingUiIntent.Next)
        advanceUntilIdle()
        assertEquals(1, preferences.writes)
        assertTrue(viewModel.state.value.failed)
        assertNull(viewModel.state.value.destination)
        preferences.fail = false
        viewModel.onIntent(OnboardingUiIntent.Finish(OnboardingDestination.Login))
        advanceUntilIdle()
        assertEquals(2, preferences.writes)
        assertEquals(OnboardingDestination.Login, viewModel.state.value.destination)
    }

    @Test fun completingAllPagesOpensLoginOnlyAfterSaving() = runTest {
        val preferences = ControlledPreferences().apply { fail = false }
        val viewModel = OnboardingViewModel(preferences, SavedStateHandle())
        repeat(3) { viewModel.onIntent(OnboardingUiIntent.Next) }
        assertNull(viewModel.state.value.destination)
        advanceUntilIdle()
        assertEquals(1, preferences.writes)
        assertEquals(OnboardingDestination.Login, viewModel.state.value.destination)
        viewModel.onIntent(OnboardingUiIntent.Next)
        advanceUntilIdle()
        assertEquals(1, preferences.writes)
    }

    private class ControlledPreferences : StartupPreferences {
        override val onboardingCompleted = flowOf(false)
        var writes = 0
        var fail = true

        override suspend fun completeOnboarding() {
            writes++
            if (fail) throw IOException()
        }
    }
}
