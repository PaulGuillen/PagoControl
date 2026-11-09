package com.example.androidbase

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidbase.features.auth.R as AuthResources
import com.example.androidbase.features.home.R as HomeResources
import com.example.androidbase.features.onboarding.R as OnboardingResources
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DemoFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun demoCanSaveAndSignOut() {
        assumeFalse(BuildConfig.FIREBASE_ENABLED)
        val activity = compose.activity
        val skip = activity.getString(OnboardingResources.string.onboarding_skip)
        val loginTitle = activity.getString(AuthResources.string.flow_login_title)
        compose.waitUntil(timeoutMillis = 10_000) {
            if (compose.onNodeWithText(skip).isDisplayed()) compose.onNodeWithText(skip).performClick()
            compose.onNodeWithText(loginTitle).isDisplayed()
        }
        compose.onNodeWithText("Acceso de demostración").assertDoesNotExist()
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_login_button)).performScrollTo().performClick()
        val title = "Sample ${UUID.randomUUID()}"
        compose.onNodeWithText(activity.getString(HomeResources.string.home_item_title)).performTextInput(title)
        compose.onNodeWithText(activity.getString(HomeResources.string.home_add)).performClick()
        val savedItem = hasText(title) and !hasSetTextAction()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(savedItem).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(savedItem).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(activity.getString(HomeResources.string.home_sign_out)).performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onNodeWithText(activity.getString(AuthResources.string.flow_login_title)).isDisplayed()
        }
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_login_title)).assertIsDisplayed()
        compose.onNodeWithText(title).assertDoesNotExist()
    }
}
