package com.devpaul.pagocontrol

import android.content.IntentFilter
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.features.home.ui.home.HomeContent
import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeSection
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeNotificationAccessUiTest {
    @get:Rule val compose = createComposeRule()

    private var access by mutableStateOf(false)
    private val content = HomeContent.Ready(Dashboard(
        balanceCents = 0, incomeCents = 0, expenseCents = 0, incomeCount = 0, expenseCount = 0,
        weeklyFlow = List(7) { 0 }, monthlyFlow = List(5) { 0 }, movements = emptyList(),
    ))
    private var state by mutableStateOf(HomeUiState(content = content, gmailContent = content))
    private val intents = mutableListOf<HomeUiIntent>()

    @Test fun missingAccessAppearsBeforeBothSectionsWithoutDuplicateButtons() {
        renderHome()
        compose.onNodeWithText("Activa el acceso a notificaciones").assertIsDisplayed()
        compose.onAllNodesWithText("Activar acceso").assertCountEquals(1)
        val permissionBottom = compose.onNodeWithText("Activar acceso").fetchSemanticsNode().boundsInRoot.bottom
        val tabsTop = compose.onNodeWithText("Correo").fetchSemanticsNode().boundsInRoot.top
        assertTrue(permissionBottom <= tabsTop)
        compose.onNodeWithText("Correo").performScrollTo().performClick()
        compose.onNodeWithText("Activa el acceso a notificaciones").assertIsDisplayed()
        compose.onAllNodesWithText("Activar acceso").assertCountEquals(1)
        assertEquals(HomeSection.Mail, state.section)
    }

    @Test fun firstGuideStepOpensSettingsAndAdvancesOnlyAfterAccessIsGranted() {
        state = state.copy(guideStep = 0)
        renderHome()
        compose.onNodeWithText("Activa el acceso a notificaciones").assertIsDisplayed()
        compose.onNodeWithText("Primero, habilita PagoControl", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Activar acceso").assertDoesNotExist()
        compose.onAllNodesWithContentDescription("Activar acceso").assertCountEquals(1)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(IntentFilter(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), null, true)
        try {
            compose.onNodeWithTag("home-guide-action").performTouchInput { click() }
            compose.runOnIdle {
                assertEquals(1, monitor.hits)
                assertEquals(0, state.guideStep)
                assertTrue(intents.isEmpty())
                access = true
            }
            compose.onNodeWithText("Guía de Inicio · 2 de 4").assertIsDisplayed()
            compose.onNodeWithText("Activa el acceso a notificaciones").assertDoesNotExist()
            compose.runOnIdle {
                assertEquals(listOf(HomeUiIntent.NextGuideStep), intents)
                assertTrue(state.notificationSources.isEmpty())
                assertEquals(false, state.gmailMonitoring)
            }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    @Test fun grantedAccessHidesTheReminderAndRevocationShowsItAgain() {
        access = true
        renderHome()
        compose.onNodeWithText("Activa el acceso a notificaciones").assertDoesNotExist()
        compose.runOnIdle { access = false }
        compose.onNodeWithText("Activa el acceso a notificaciones").assertIsDisplayed()
        compose.runOnIdle { access = true }
        compose.onNodeWithText("Activa el acceso a notificaciones").assertDoesNotExist()
        compose.onNodeWithText("COBROS RECIBIDOS ESTE MES").assertIsDisplayed()
    }

    @Test fun permissionGuideActionsFitOnCompactScreensWithLargeText() {
        state = state.copy(guideStep = 0)
        renderHome(compact = true)
        compose.onNodeWithText("Cancelar").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").assertIsDisplayed()
        compose.onNodeWithTag("home-guide-action").assertIsDisplayed()
        compose.onAllNodesWithContentDescription("Activar acceso").assertCountEquals(1)
        compose.onNodeWithText("Activar acceso").assertDoesNotExist()
    }

    @Test fun walletGuideOpensThePickerAllowsSelectionAndReturnsToTheSameStep() {
        access = true
        state = state.copy(guideStep = 1, notificationSources = (1..5).map { "test.wallet.$it" }.toSet())
        renderHome()
        compose.onNodeWithTag("home-guide-action").performTouchInput { click() }
        compose.onNodeWithText("Elegir apps").assertIsDisplayed()
        compose.onNodeWithTag("home-guide-overlay").assertDoesNotExist()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.packageManager
        val app = manager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .first { it.activityInfo.packageName != context.packageName && it.activityInfo.packageName != "com.google.android.gm" }
        compose.onNode(hasSetTextAction()).performTextInput(app.loadLabel(manager).toString())
        val tag = "monitored-app-${app.activityInfo.packageName}"
        compose.waitUntil(5_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(tag).performClick()
        compose.runOnIdle { assertTrue(app.activityInfo.packageName in state.notificationSources) }
        compose.onNodeWithText("Listo").performClick()
        compose.onNodeWithTag("home-guide-overlay").assertIsDisplayed()
        compose.onNodeWithText("Guía de Inicio · 2 de 4").assertIsDisplayed()
    }

    @Test fun gmailGuideTogglesTheSwitchButRespectsItsSavingState() {
        access = true
        state = state.copy(section = HomeSection.Mail, guideStep = 1)
        renderHome()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("home-guide-action").fetchSemanticsNodes().any {
                !it.config.contains(SemanticsProperties.Disabled)
            }
        }
        compose.onNodeWithTag("home-guide-action").assertIsOff().performTouchInput { click() }
        compose.onNodeWithTag("home-guide-action").assertIsOn()
        compose.runOnIdle {
            assertEquals(listOf(HomeUiIntent.SetGmailMonitoring(true)), intents)
            state = state.copy(savingNotification = true)
        }
        compose.onNodeWithTag("home-guide-action").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(1, intents.size)
            state = state.copy(savingNotification = false)
        }
        compose.onNodeWithTag("home-guide-action").performTouchInput { click() }
        compose.onNodeWithTag("home-guide-action").assertIsOff()
        compose.runOnIdle { assertEquals(HomeUiIntent.SetGmailMonitoring(false), intents.last()) }
    }

    @Test fun unrelatedGuideStepsAndBackgroundRemainReadOnly() {
        access = true
        state = state.copy(guideStep = 2)
        renderHome()
        compose.onNodeWithTag("home-guide-action").assertDoesNotExist()
        compose.onNodeWithText("Semana").assertDoesNotExist()
        compose.onNodeWithTag("home-guide-overlay").performTouchInput { click(Offset(4f, 4f)) }
        compose.runOnIdle {
            assertTrue(intents.isEmpty())
            assertEquals(2, state.guideStep)
        }
    }

    private fun renderHome(compact: Boolean = false) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, if (compact) 1.5f else density.fontScale)) {
                PagoControlTheme(false) {
                    Box(if (compact) Modifier.height(420.dp) else Modifier) {
                        HomeScreen(
                            state = state,
                            name = "Prueba",
                            onIntent = { intent ->
                                intents += intent
                                when (intent) {
                                    is HomeUiIntent.SelectSection -> state = state.copy(section = intent.section)
                                    HomeUiIntent.NextGuideStep -> state = state.copy(guideStep = state.guideStep?.plus(1))
                                    is HomeUiIntent.SetNotificationSource -> state = state.copy(
                                        notificationSources = if (intent.enabled) state.notificationSources + intent.packageName else state.notificationSources - intent.packageName,
                                    )
                                    is HomeUiIntent.SetGmailMonitoring -> state = state.copy(gmailMonitoring = intent.enabled)
                                    else -> Unit
                                }
                            },
                            themeIndex = 1,
                            onThemeSelected = {},
                            notificationAccessGranted = access,
                        )
                    }
                }
            }
        }
    }
}
