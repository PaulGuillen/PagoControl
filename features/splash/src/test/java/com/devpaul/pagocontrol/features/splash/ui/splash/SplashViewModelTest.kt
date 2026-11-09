package com.devpaul.pagocontrol.features.splash.ui.splash

import com.devpaul.pagocontrol.domain.StartupPreferences
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
class SplashViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun retriesStartupReadWithoutDiscardingPreference() = runTest {
        val preferences = ControlledPreferences()
        val viewModel = SplashViewModel(preferences)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.failed)
        assertNull(viewModel.state.value.onboardingCompleted)
        preferences.fail = false
        viewModel.onIntent(SplashUiIntent.Retry)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.failed)
        assertEquals(true, viewModel.state.value.onboardingCompleted)
    }

    private class ControlledPreferences : StartupPreferences {
        var fail = true
        override val onboardingCompleted get() = flow {
            if (fail) throw IOException()
            emit(true)
        }
        override suspend fun completeOnboarding() = Unit
    }
}
