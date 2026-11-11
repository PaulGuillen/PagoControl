package com.devpaul.features.home

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeRepository
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.features.home.ui.AllTemplatesScreen
import com.devpaul.features.home.ui.HomeScreen
import com.devpaul.features.templates.ui.TemplateExamplePreviewScreen
import com.devpaul.navigation.route.HomeRoute
import kotlinx.coroutines.flow.StateFlow

fun NavGraphBuilder.homeGraph(
    repository: ResumeRepository,
    userProfile: StateFlow<UserProfileUiState>,
    onNavigateToMyCvs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onPreviewTemplate: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onContinueResume: (ResumeDocument) -> Unit,
    onNavigateToNotifications: () -> Unit,
    isPremium: Boolean = false,
    onPreviewResume: (ResumeDocument) -> Unit = { onNavigateToMyCvs() },
) {
    navigation<HomeRoute.Graph>(
        startDestination = HomeRoute.Home,
    ) {
        composable<HomeRoute.Home> {
            val resumes by repository.resumes.collectAsState(initial = emptyList())
            val profileState by userProfile.collectAsStateWithLifecycle()
            HomeScreen(
                onNavigateToMyCvs = onNavigateToMyCvs,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToTemplates = onNavigateToTemplates,
                onPreviewTemplate = onPreviewTemplate,
                userProfile = profileState,
                recentResume = resumes.maxByOrNull(ResumeDocument::updatedAt),
                onContinueResume = onContinueResume,
                onNavigateToNotifications = onNavigateToNotifications,
                isPremium = isPremium,
                onPreviewResume = onPreviewResume,
            )
        }
        composable<HomeRoute.Templates> {
            AllTemplatesScreen(
                onBack = onNavigateBack,
                onPreviewTemplate = onPreviewTemplate,
            )
        }
        composable<HomeRoute.TemplatePreview> { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.TemplatePreview>()
            TemplateExamplePreviewScreen(
                templateId = route.templateId,
                onBack = onNavigateBack,
            )
        }
    }
}
