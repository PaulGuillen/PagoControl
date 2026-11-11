package com.devpaul.cvfree.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.cvfree.R
import com.devpaul.navigation.route.HomeRoute
import com.devpaul.navigation.route.ResumeRoute
import com.devpaul.navigation.route.SettingsRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LogoutConfirmationTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var controller: NavHostController
    private var confirmed = 0

    private fun label(resource: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(resource)

    private fun showContent(darkTheme: Boolean = false) {
        compose.setContent {
            CvFreeTheme(darkTheme = darkTheme) {
                controller = rememberNavController()
                val entry by controller.currentBackStackEntryAsState()
                var showConfirmation by remember { mutableStateOf(false) }
                NavHost(controller, startDestination = HomeRoute.Home) {
                    composable<HomeRoute.Home> { Text("Home") }
                    composable<ResumeRoute.ResumeList> { Text("CVs") }
                    composable<SettingsRoute.Settings> { Text("Settings") }
                    composable<ResumeRoute.Contact> { Text("Contact") }
                }
                MainScreenBackHandler(entry?.destination) { showConfirmation = true }
                if (showConfirmation) {
                    LogoutConfirmationDialog(
                        onConfirm = { confirmed++; showConfirmation = false },
                        onDismiss = { showConfirmation = false },
                    )
                }
            }
        }
    }

    @Test
    fun backFromEachMainScreenRequiresConfirmationAndCancelKeepsTheScreen() {
        showContent()
        for (route in listOf(HomeRoute.Home, ResumeRoute.ResumeList, SettingsRoute.Settings)) {
            compose.runOnIdle { controller.navigate(route) }
            pressBack()
            compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertIsDisplayed()
            compose.onNodeWithText(label(R.string.logout_confirmation_cancel)).performClick()
            compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertDoesNotExist()
            compose.runOnIdle { assertEquals(0, confirmed) }
        }
    }

    @Test
    fun confirmingSignOutCallsTheActionOnce() {
        showContent()
        pressBack()
        compose.onNodeWithText(label(R.string.logout_confirmation_confirm)).performClick()
        compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, confirmed) }
    }

    @Test
    fun backDismissesTheDialogWithoutSigningOut() {
        showContent()
        pressBack()
        compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertIsDisplayed()
        pressBack()
        compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, confirmed) }
    }

    @Test
    fun innerScreensKeepTheirNormalBackNavigation() {
        showContent()
        compose.runOnIdle { controller.navigate(ResumeRoute.Contact) }
        pressBack()
        compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertDoesNotExist()
        compose.runOnIdle {
            assertTrue(controller.currentDestination?.hasRoute<HomeRoute.Home>() == true)
            assertEquals(0, confirmed)
        }
    }

    @Test
    fun darkThemeShowsBothConfirmationActions() {
        showContent(darkTheme = true)
        pressBack()
        compose.onNodeWithText(label(R.string.logout_confirmation_title)).assertIsDisplayed()
        compose.onNodeWithText(label(R.string.logout_confirmation_confirm)).assertIsDisplayed()
        compose.onNodeWithText(label(R.string.logout_confirmation_cancel)).assertIsDisplayed()
    }
}
