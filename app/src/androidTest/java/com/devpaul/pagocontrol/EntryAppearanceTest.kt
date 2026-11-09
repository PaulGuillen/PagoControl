package com.devpaul.pagocontrol

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.designsystem.EntryColors
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.features.auth.authGraph
import com.devpaul.pagocontrol.features.onboarding.onboardingGraph
import com.devpaul.pagocontrol.features.onboarding.ui.onboarding.OnboardingScreen
import com.devpaul.pagocontrol.features.onboarding.ui.onboarding.OnboardingUiState
import com.devpaul.pagocontrol.features.splash.ui.splash.SplashScreen
import com.devpaul.pagocontrol.features.splash.ui.splash.SplashUiState
import com.devpaul.pagocontrol.navigation.route.AuthRoute
import com.devpaul.pagocontrol.navigation.route.OnboardingRoute
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntryAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun splashDark() = captureSplash(true)
    @Test fun splashLight() = captureSplash(false)
    @Test fun onboardingDark() = captureOnboarding(true)
    @Test fun onboardingLight() = captureOnboarding(false)
    @Test fun privacyDark() = captureOnboarding(dark = true, page = 1)
    @Test fun privacyLight() = captureOnboarding(dark = false, page = 1)
    @Test fun controlDark() = captureOnboarding(dark = true, page = 2)
    @Test fun controlLight() = captureOnboarding(dark = false, page = 2)

    @Test fun providerColorsSupportWhiteLabels() {
        listOf(EntryColors.Purple, EntryColors.Cyan, EntryColors.Green, EntryColors.Blue, EntryColors.Red, EntryColors.Amber).forEach { color ->
            assertTrue(1.05f / (color.luminance() + 0.05f) >= 4.5f)
        }
    }

    @Test fun finishingAllStepsNavigatesToLogin() {
        compose.setContent {
            PagoControlTheme(false) {
                val controller = rememberNavController()
                NavHost(navController = controller, startDestination = OnboardingRoute.Graph) {
                    onboardingGraph {
                        controller.navigate(AuthRoute.Graph) {
                            popUpTo(controller.graph.id) { inclusive = true }
                        }
                    }
                    authGraph(
                        themeIndex = 1,
                        onThemeSelected = {},
                        onRegister = {},
                        onForgotPassword = {},
                        onLogin = {},
                    )
                }
            }
        }
        repeat(2) { compose.onNodeWithText("Siguiente").performScrollTo().performClick() }
        compose.onNodeWithText("Comenzar ahora").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Bienvenido de nuevo").assertIsDisplayed()
    }

    private fun captureSplash(dark: Boolean) {
        compose.setContent { PagoControlTheme(dark) { SplashScreen(SplashUiState(), {}) } }
        compose.onNodeWithText("SINCRONIZACIÓN INTELIGENTE").assertIsDisplayed()
        saveImage(if (dark) "splash-dark" else "splash-light")
    }

    private fun captureOnboarding(dark: Boolean, page: Int = 0) {
        compose.setContent { PagoControlTheme(dark) { OnboardingScreen(OnboardingUiState(page = page), {}) } }
        val visibleLabel = when (page) {
            1 -> "BCP / BBVA"
            2 -> "TOTAL DISPONIBLE"
            else -> "Carlos M."
        }
        compose.onNodeWithText(visibleLabel).assertIsDisplayed()
        val screen = when (page) { 1 -> "privacy"; 2 -> "control"; else -> "onboarding" }
        saveImage("$screen-${if (dark) "dark" else "light"}")
    }

    private fun saveImage(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "entry-previews").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { output ->
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}
