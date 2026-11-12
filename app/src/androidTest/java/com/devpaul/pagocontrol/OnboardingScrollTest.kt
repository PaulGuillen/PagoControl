package com.devpaul.pagocontrol

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.features.onboarding.ui.onboarding.OnboardingScreen
import com.devpaul.pagocontrol.features.onboarding.ui.onboarding.OnboardingUiIntent
import com.devpaul.pagocontrol.features.onboarding.ui.onboarding.OnboardingUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScrollTest {
    @get:Rule val compose = createComposeRule()

    @Test fun everyPageStartsAtTheTopIncludingWhenGoingBack() {
        var state by mutableStateOf(OnboardingUiState())
        compose.setContent {
            PagoControlTheme(false) {
                Box(Modifier.height(420.dp)) {
                    OnboardingScreen(state) { intent ->
                        if (intent == OnboardingUiIntent.Next) state = state.copy(page = state.page + 1)
                    }
                }
            }
        }
        compose.onNodeWithText("Omitir").assertIsDisplayed()
        repeat(2) {
            compose.onNodeWithText("Siguiente").performScrollTo().performClick()
            compose.onNodeWithText("Omitir").assertIsDisplayed()
        }
        compose.onNodeWithText("Comenzar ahora").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { state = state.copy(page = 1) }
        compose.onNodeWithText("Omitir").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performScrollTo()
        compose.runOnIdle { state = state.copy(page = 0) }
        compose.onNodeWithText("Omitir").assertIsDisplayed()
    }
}
