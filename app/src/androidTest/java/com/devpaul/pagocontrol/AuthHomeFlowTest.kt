package com.devpaul.pagocontrol

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import androidx.compose.ui.test.isDisplayed
import com.devpaul.pagocontrol.features.auth.R as AuthResources
import com.devpaul.pagocontrol.features.onboarding.R as OnboardingResources
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthHomeFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun enterLogin() {
        val loginLabel = compose.activity.getString(AuthResources.string.flow_login_title)
        val skipLabel = compose.activity.getString(OnboardingResources.string.onboarding_skip)
        compose.waitUntil(10_000) {
            if (compose.onNodeWithText(skipLabel).isDisplayed()) compose.onNodeWithText(skipLabel).performClick()
            compose.onNodeWithText(loginLabel).isDisplayed()
        }
    }

    @Test fun loginThemeRecreationAndMovements() {
        compose.onNodeWithText(compose.activity.getString(AuthResources.string.flow_login_button)).performScrollTo().performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("BALANCE DISPONIBLE")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("S/ 4,580.50").assertIsDisplayed()
        compose.onNodeWithContentDescription("Notificaciones").assertIsDisplayed()
        compose.onNodeWithContentDescription("Cambiar apariencia").assertDoesNotExist()
        compose.onNodeWithContentDescription("Cerrar sesión").assertDoesNotExist()
        compose.onNodeWithText("Movimientos").performClick()
        compose.onNodeWithText("Balance Mensualizado").assertIsDisplayed()
        compose.onNodeWithText("María Rodríguez").performScrollTo().assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Balance Mensualizado").assertIsDisplayed()
        compose.onNodeWithText("Ingresos  4").performClick()
        compose.onNodeWithText("María Rodríguez").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Inicio").performClick()
        compose.onNodeWithText("BALANCE DISPONIBLE").assertIsDisplayed()
        compose.onNodeWithContentDescription("Agregar movimiento").assertDoesNotExist()
        compose.onNodeWithText("Reportes").performClick()
        compose.onNodeWithText("BALANCE NETO DEL PERÍODO").assertIsDisplayed()
        compose.onNodeWithText("Últimos 3 meses").performClick()
        compose.onNodeWithText("Septiembre – Noviembre 2023").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Septiembre – Noviembre 2023").assertIsDisplayed()
        compose.onNodeWithText("Movimientos").performClick()
        compose.onNodeWithText("Balance Mensualizado").assertIsDisplayed()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Mi perfil").assertIsDisplayed()
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText("Nombre").performTextReplacement("Carlos Perfil")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Carlos Perfil").assertIsDisplayed()
        compose.onNodeWithText("Plan Free").assertDoesNotExist()
        compose.onNodeWithText("Apariencia").performScrollTo().performClick()
        compose.onAllNodesWithText("Claro").onLast().performClick()
        compose.onNodeWithText("Notificaciones", useUnmergedTree = true).performScrollTo()
        val before = compose.onNode(isToggleable()).fetchSemanticsNode().config[SemanticsProperties.ToggleableState]
        compose.onNode(isToggleable()).performClick()
        compose.waitForIdle()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Notificaciones", useUnmergedTree = true).performScrollTo()
        compose.onNode(isToggleable()).assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, if (before == ToggleableState.On) ToggleableState.Off else ToggleableState.On),
        )
        compose.onNode(isToggleable()).performClick()
        compose.onNodeWithText("Centro de ayuda").performScrollTo().performClick()
        compose.onNodeWithText("¿Cómo podemos ayudarte?").assertIsDisplayed()
        compose.onNodeWithText("¿Cómo encuentro un movimiento?").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Volver").performScrollTo().performClick()
        compose.onNodeWithText("Privacidad").performScrollTo().performClick()
        compose.onNodeWithText("Tu información, con claridad").assertIsDisplayed()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Calificar PagoControl").performScrollTo().performClick()
        compose.onNodeWithContentDescription("4 / 5 estrellas").performClick()
        compose.onNodeWithText("Comentario (opcional)").performTextInput("Muy útil")
        compose.onNodeWithText("Guardar valoración").performClick()
        compose.onNodeWithText("¡Gracias por tu opinión! Tu valoración se guardó en este dispositivo.").assertIsDisplayed()
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Cerrar sesión"))
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.onAllNodesWithText("Cerrar sesión").onLast().performClick()
        compose.waitUntil(5_000) {
            compose.onNodeWithText(compose.activity.getString(AuthResources.string.flow_login_button)).isDisplayed()
        }
    }
}
