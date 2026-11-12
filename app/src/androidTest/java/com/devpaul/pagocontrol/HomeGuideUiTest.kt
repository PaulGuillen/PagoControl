package com.devpaul.pagocontrol

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.features.home.ui.home.HomeContent
import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeSection
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeGuideUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun walletGuideScrollsToEachControlAndCanBeReplayed() {
        showGuide(HomeSection.Wallets)
        compose.onNodeWithTag("home-guide-overlay").assertIsDisplayed()
        compose.onNodeWithTag("home-guide-overlay").performTouchInput { click(Offset(4f, 4f)) }
        compose.onNodeWithText("Guía de Inicio · 1 de 4").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Guía de Inicio · 2 de 4").assertIsDisplayed()
        compose.onNodeWithText("Elegir apps a monitorear").assertDoesNotExist()
        compose.onNodeWithTag("home-guide-tooltip").assertIsDisplayed()
        saveImage("wallet-monitoring-overlay")
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Guía de Inicio · 3 de 4").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Guía de Inicio · 4 de 4").assertIsDisplayed()
        compose.onNodeWithText("Entendido").performClick()
        compose.onNodeWithTag("home-guide-overlay").assertDoesNotExist()
        compose.onNodeWithText("¿Cómo funciona Inicio?").performScrollTo().performClick()
        compose.onNodeWithText("Guía de Inicio · 1 de 4").assertIsDisplayed()
    }

    @Test fun gmailGuideExplainsItsOwnSwitchRatherThanTheWalletPicker() {
        showGuide(HomeSection.Mail)
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Guía de Inicio · 2 de 4").assertIsDisplayed()
        compose.onNodeWithText("Activa Monitorear Gmail.", substring = true).assertIsDisplayed()
        saveImage("gmail-monitoring-overlay")
        compose.onNodeWithContentDescription("Monitorear Gmail").assertExists()
        compose.onNodeWithText("Elegir apps a monitorear").assertDoesNotExist()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithTag("home-guide-overlay").assertDoesNotExist()
        compose.onNodeWithText("Guía de Inicio · 2 de 4").assertDoesNotExist()
    }

    @Test fun overlayKeepsActionsVisibleOnSmallScreensWithLargeText() {
        showGuide(section = HomeSection.Wallets, compact = true)
        repeat(3) {
            compose.onNodeWithText("Cancelar").assertIsDisplayed()
            compose.onNodeWithText("Siguiente").assertIsDisplayed().performClick()
        }
        compose.onNodeWithText("Cancelar").assertIsDisplayed()
        compose.onNodeWithText("Entendido").assertIsDisplayed()
        saveImage("compact-dark-overlay")
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithTag("home-guide-overlay").assertDoesNotExist()
    }

    private fun saveImage(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "home-guide-previews").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { output ->
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    private fun showGuide(section: HomeSection, compact: Boolean = false) {
        val content = HomeContent.Ready(Dashboard(
            balanceCents = 0, incomeCents = 0, expenseCents = 0, incomeCount = 0, expenseCount = 0,
            weeklyFlow = List(7) { 0 }, monthlyFlow = List(5) { 0 }, movements = emptyList(),
        ))
        var state by mutableStateOf(HomeUiState(content = content, gmailContent = content, section = section, guideStep = 0))
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, if (compact) 1.5f else density.fontScale)) {
                PagoControlTheme(compact) {
                    Box(if (compact) Modifier.height(420.dp) else Modifier) {
                        HomeScreen(
                            state = state,
                            name = "Prueba",
                            onIntent = { intent ->
                                state = when (intent) {
                                    HomeUiIntent.ShowGuide -> state.copy(guideStep = 0)
                                    HomeUiIntent.NextGuideStep -> state.copy(guideStep = state.guideStep?.let { if (it == 3) null else it + 1 })
                                    HomeUiIntent.PreviousGuideStep -> state.copy(guideStep = state.guideStep?.minus(1))
                                    HomeUiIntent.DismissGuide -> state.copy(guideStep = null)
                                    else -> state
                                }
                            },
                            themeIndex = 1,
                            onThemeSelected = {},
                            notificationAccessGranted = true,
                        )
                    }
                }
            }
        }
    }
}
