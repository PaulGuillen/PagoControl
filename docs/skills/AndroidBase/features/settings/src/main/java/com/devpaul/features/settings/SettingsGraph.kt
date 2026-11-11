package com.devpaul.features.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.devpaul.features.settings.ui.SettingsScreen
import com.devpaul.features.settings.ui.MyPlanScreen
import com.devpaul.features.settings.ui.PremiumFeedbackViewModel
import com.devpaul.features.settings.ui.HelpCenterScreen
import com.devpaul.features.settings.ui.PrivacyScreen
import com.devpaul.features.settings.ui.RatingViewModel
import com.devpaul.features.settings.ui.components.RatingDialog
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.theme.AppAppearance
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.navigation.route.SettingsRoute
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.settingsGraph(
    onNavigateToHome: () -> Unit,
    onNavigateToMyCvs: () -> Unit,
    onLogout: () -> Unit,
    userProfile: StateFlow<UserProfileUiState>,
    isPremium: Boolean,
    onUpdateName: suspend (String) -> String?,
    appLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    appAppearance: AppAppearance,
    onAppearanceSelected: (AppAppearance) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    autoSaveEnabled: Boolean,
    onAutoSaveEnabledChange: (Boolean) -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMyPlan: () -> Unit,
    versionName: String = "",
) {
    navigation<SettingsRoute.Graph>(
        startDestination = SettingsRoute.Settings,
    ) {
        composable<SettingsRoute.Settings> {
            val ratingViewModel: RatingViewModel = koinViewModel()
            val ratingState by ratingViewModel.state.collectAsStateWithLifecycle()
            val profileState by userProfile.collectAsStateWithLifecycle()
            SettingsScreen(
                versionName = versionName,
                onNavigateToHome = onNavigateToHome,
                onNavigateToMyCvs = onNavigateToMyCvs,
                onLogout = onLogout,
                userName = profileState.name,
                userEmail = profileState.email,
                isProfileLoading = profileState.isLoading,
                isPremium = isPremium,
                onNavigateToMyPlan = onNavigateToMyPlan,
                onUpdateName = onUpdateName,
                appLanguage = appLanguage,
                appAppearance = appAppearance,
                onAppearanceSelected = onAppearanceSelected,
                notificationsEnabled = notificationsEnabled,
                autoSaveEnabled = autoSaveEnabled,
                onAutoSaveEnabledChange = onAutoSaveEnabledChange,
                onNotificationsEnabledChange = onNotificationsEnabledChange,
                onLanguageSelected = onLanguageSelected,
                onNavigateToHelp = onNavigateToHelp,
                onRateApp = ratingViewModel::open,
                onNavigateToPrivacy = onNavigateToPrivacy,
            )
            RatingDialog(
                state = ratingState,
                onStarsSelected = ratingViewModel::selectStars,
                onCommentChange = ratingViewModel::updateComment,
                onSubmit = ratingViewModel::submit,
                onDismiss = ratingViewModel::dismiss,
            )
        }
        composable<SettingsRoute.MyPlan> {
            val feedbackViewModel: PremiumFeedbackViewModel = koinViewModel()
            val feedbackState by feedbackViewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(feedbackViewModel) { feedbackViewModel.open() }
            MyPlanScreen(
                feedbackState = feedbackState,
                isPremium = isPremium,
                onBack = onNavigateBack,
                onStarsSelected = feedbackViewModel::selectStars,
                onCommentChange = feedbackViewModel::updateComment,
                onSubmit = feedbackViewModel::submit,
            )
        }
        composable<SettingsRoute.HelpCenter> {
            HelpCenterScreen(onBack = onNavigateBack)
        }
        composable<SettingsRoute.Privacy> {
            PrivacyScreen(onBack = onNavigateBack)
        }
    }
}
