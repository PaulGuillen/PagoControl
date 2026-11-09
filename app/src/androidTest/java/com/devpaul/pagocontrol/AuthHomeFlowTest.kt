package com.devpaul.pagocontrol

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthHomeFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun loginThemeRecreationAndLogout() {
        compose.onNodeWithText("Completar acceso de demostración").performClick()
        compose.onNodeWithText("Ingresar a la demo").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("BALANCE DISPONIBLE")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("S/ 4,580.50").assertIsDisplayed()
        compose.onNodeWithContentDescription("Cambiar apariencia").performClick()
        compose.onNodeWithText("Modo oscuro").performClick()
        compose.waitForIdle()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("BALANCE DISPONIBLE").assertIsDisplayed()
        compose.onNodeWithContentDescription("Cambiar apariencia").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Modo oscuro"),
        )
        compose.onNodeWithContentDescription("Cerrar sesión").performClick()
        compose.onNodeWithText("Ingresar a la demo").assertIsDisplayed()
        compose.onNodeWithText("Completar acceso de demostración").performClick()
        compose.onNodeWithText("Ingresar a la demo").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("BALANCE DISPONIBLE")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Cerrar sesión").performClick()
        compose.onNodeWithContentDescription("Cambiar apariencia").performClick()
        compose.onNodeWithText("Usar tema del sistema").performClick()
    }
}
