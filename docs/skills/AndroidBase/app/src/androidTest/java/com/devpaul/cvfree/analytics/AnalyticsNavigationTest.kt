package com.devpaul.cvfree.analytics

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.core.platform.analytics.AnalyticsReporter
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.features.home.ui.HomeScreen
import com.devpaul.navigation.route.AuthRoute
import com.devpaul.navigation.route.ResumeRoute
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AnalyticsNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = CopyOnWriteArrayList<Pair<String, Map<String, String>>>()

    @Before
    fun prepare() {
        AppAnalytics.initialize(object : AnalyticsReporter {
            override fun logEvent(name: String, parameters: Map<String, String>) {
                events += name to parameters
            }

            override fun recordError(operation: String, error: Throwable) = Unit
        })
    }

    @Test
    fun parameterizedDestinationsReportFixedScreenNamesWithoutArguments() {
        lateinit var controller: NavHostController
        compose.setContent {
            controller = rememberNavController()
            NavHost(controller, startDestination = AuthRoute.PasswordResetSent("private@example.com")) {
                composable<AuthRoute.PasswordResetSent> { entry ->
                    LaunchedEffect(entry) { entry.destination.analyticsScreenName()?.let(AppAnalytics::screenView) }
                    Text("Reset")
                }
                composable<ResumeRoute.Importing> { entry ->
                    LaunchedEffect(entry) { entry.destination.analyticsScreenName()?.let(AppAnalytics::screenView) }
                    Text("Import")
                }
            }
        }
        compose.runOnIdle { controller.navigate(ResumeRoute.Importing("content://private/cv.pdf")) }
        compose.waitForIdle()

        val screenEvents = events.filter { it.first == "screen_view" }
        assertEquals(listOf("password_reset_sent", "resume_import"), screenEvents.map { it.second["screen_name"] })
        assertFalse(screenEvents.any { it.second.values.any { value -> "private" in value } })
    }

    @Test
    fun notificationButtonReportsOneClickAndOpensHistory() {
        var opened = 0
        AppAnalytics.screenView("home")
        events.clear()
        compose.setContent {
            CvFreeTheme {
                HomeScreen(
                    onNavigateToMyCvs = {},
                    onNavigateToSettings = {},
                    onNavigateToTemplates = {},
                    onPreviewTemplate = {},
                    onContinueResume = {},
                    onNavigateToNotifications = { opened++ },
                )
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithContentDescription(context.getString(com.devpaul.features.home.R.string.home_notifications))
            .performClick()
        compose.runOnIdle {
            val click = events.filter { it.first == "ui_click" }.single()
            assertEquals("open_notifications", click.second["action"])
            assertEquals("home", click.second["screen_name"])
            assertEquals(1, opened)
        }
    }
}
