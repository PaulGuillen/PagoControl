package com.devpaul.pagocontrol

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devpaul.pagocontrol.features.auth.R as AuthResources
import com.devpaul.pagocontrol.features.onboarding.R as OnboardingResources
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountScreensFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun registerAndRecoveryValidateAndReturnToLogin() {
        val activity = compose.activity
        val skip = activity.getString(OnboardingResources.string.onboarding_skip)
        val loginTitle = activity.getString(AuthResources.string.flow_login_title)
        compose.waitUntil(10_000) { compose.onNodeWithText(skip).isDisplayed() || compose.onNodeWithText(loginTitle).isDisplayed() }
        if (compose.onNodeWithText(skip).isDisplayed()) compose.onNodeWithText(skip).performClick()
        compose.waitUntil(10_000) { compose.onNodeWithText(loginTitle).isDisplayed() }

        compose.onNodeWithText(activity.getString(AuthResources.string.flow_register_link)).performScrollTo().performClick()
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_create_account)).performScrollTo().performClick()
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_register_invalid)).performScrollTo().assertIsDisplayed()
        compose.onNode(hasText(activity.getString(AuthResources.string.flow_name)) and hasSetTextAction()).performScrollTo().performTextInput("Paul Demo")
        compose.onNode(hasText(activity.getString(AuthResources.string.flow_email)) and hasSetTextAction()).performScrollTo().performTextInput("demo@test.pe")
        compose.onNode(hasText(activity.getString(AuthResources.string.flow_password)) and hasSetTextAction()).performScrollTo().performTextInput("Password123")
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_accept_terms)).performScrollTo().performClick()
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_create_account)).performScrollTo().performClick()
        val registerDemo = activity.getString(AuthResources.string.flow_register_demo)
        compose.waitUntil(10_000) { compose.onNodeWithText(registerDemo).isDisplayed() }
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_back_login)).performScrollTo().performClick()

        compose.onNodeWithText(activity.getString(AuthResources.string.flow_forgot_link)).performScrollTo().performClick()
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_send_instructions)).performScrollTo().performClick()
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_email_invalid)).performScrollTo().assertIsDisplayed()
        compose.onNode(hasContentDescription(activity.getString(AuthResources.string.flow_email)) and hasSetTextAction()).performScrollTo().performTextInput("demo@test.pe")
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_send_instructions)).performScrollTo().performClick()
        val recoveryDemo = activity.getString(AuthResources.string.flow_forgot_demo)
        compose.waitUntil(10_000) { compose.onNodeWithText(recoveryDemo).isDisplayed() }
        compose.onNodeWithText(activity.getString(AuthResources.string.flow_back_login)).performScrollTo().performClick()
        compose.onNodeWithText(loginTitle).performScrollTo().assertIsDisplayed()
    }
}
