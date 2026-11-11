package com.devpaul.cvfree

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.devpaul.core.domain.resume.ResumeRepository
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.cvfree.analytics.analyticsScreenName
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.theme.AppAppearance
import com.devpaul.cvfree.access.FeatureAccessController
import com.devpaul.cvfree.access.AccessPlan
import com.devpaul.cvfree.notifications.NotificationHistoryStore
import com.devpaul.cvfree.notifications.NotificationHistoryScreen
import com.devpaul.cvfree.ui.LogoutConfirmationDialog
import com.devpaul.cvfree.ui.MainScreenBackHandler
import com.devpaul.features.auth.authGraph
import com.devpaul.features.auth.ui.session.AuthSessionViewModel
import com.devpaul.features.home.homeGraph
import com.devpaul.features.onboarding.onboardingGraph
import com.devpaul.features.resume.ResumeDraftState
import com.devpaul.features.resume.importing.ResumeImportPolicy
import com.devpaul.features.resume.resumeGraph
import com.devpaul.features.settings.settingsGraph
import com.devpaul.features.splash.splashGraph
import com.devpaul.navigation.route.AuthRoute
import com.devpaul.navigation.route.HomeRoute
import com.devpaul.navigation.route.OnboardingRoute
import com.devpaul.navigation.route.ResumeRoute
import com.devpaul.navigation.route.SettingsRoute
import com.devpaul.navigation.route.SplashRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainGraph(
    navController: NavHostController,
    appLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    appAppearance: AppAppearance,
    onAppearanceSelected: (AppAppearance) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    autoSaveEnabled: Boolean,
    onAutoSaveEnabledChange: (Boolean) -> Unit,
    onResumeSaved: () -> Unit,
    notificationHistory: NotificationHistoryStore,
    openMyCvsRequested: Boolean,
    onOpenMyCvsHandled: () -> Unit,
) {
    val resources = LocalResources.current
    val resumeRepository: ResumeRepository = koinInject()
    val authSessionViewModel: AuthSessionViewModel = koinViewModel()
    val featureAccessController: FeatureAccessController = koinInject()
    val featureAccess by featureAccessController.state.collectAsStateWithLifecycle()
    val resumeDraftState: ResumeDraftState = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    SideEffect {
        resumeDraftState.configureAutoSave(resumeRepository, autoSaveEnabled)
    }
    DisposableEffect(lifecycleOwner, resumeDraftState) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) resumeDraftState.flushAutoSave()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            resumeDraftState.flushAutoSave()
        }
    }
    LaunchedEffect(resumeDraftState.autoSaveErrorCount) {
        if (resumeDraftState.autoSaveErrorCount > 0) {
            Toast.makeText(
                context,
                resources.getString(com.devpaul.features.resume.R.string.resume_autosave_error),
                Toast.LENGTH_LONG,
            ).show()
        }
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(currentBackStackEntry) {
        showLogoutConfirmation = false
        currentBackStackEntry?.destination?.analyticsScreenName()?.let(AppAnalytics::screenView)
    }
    val resumeImportPolicy = ResumeImportPolicy(
        aiEnhancementEnabled = featureAccess.aiResumeImportEnabled,
        isFreePlan = featureAccess.plan == AccessPlan.Free,
    )

    NavHost(
        navController = navController,
        startDestination = SplashRoute.Graph,
    ) {
        splashGraph(
            isPremium = featureAccess.plan == AccessPlan.Premium,
            versionName = BuildConfig.VERSION_NAME,
            onFinished = {
                val destination = if (authSessionViewModel.isAuthenticated()) {
                    HomeRoute.Graph
                } else {
                    OnboardingRoute.Graph
                }
                navController.navigate(destination) {
                    popUpTo<SplashRoute.Graph> { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        onboardingGraph(
            onFinished = {
                navController.navigate(AuthRoute.Graph) {
                    popUpTo<OnboardingRoute.Graph> { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        authGraph(
            versionName = BuildConfig.VERSION_NAME,
            isPremium = featureAccess.plan == AccessPlan.Premium,
            appLanguage = appLanguage,
            onLanguageSelected = onLanguageSelected,
            onAuthenticated = {
                authSessionViewModel.refreshUser()
                navController.navigate(HomeRoute.Graph) {
                    popUpTo<AuthRoute.Graph> { inclusive = true }
                    launchSingleTop = true
                }
            },
            onForgotPassword = {
                navController.navigate(AuthRoute.ForgotPassword) {
                    launchSingleTop = true
                }
            },
            onPasswordResetSent = { email ->
                navController.navigate(AuthRoute.PasswordResetSent(email)) {
                    popUpTo<AuthRoute.ForgotPassword> { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBackFromForgotPassword = {
                navController.popBackStack()
            },
            onBackToLogin = {
                navController.navigate(AuthRoute.Login) {
                    popUpTo<AuthRoute.Graph> { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigateToRegister = {
                navController.navigate(AuthRoute.Register) {
                    launchSingleTop = true
                }
            },
            onNavigateToLogin = {
                navController.popBackStack()
            },
        )
        homeGraph(
            isPremium = featureAccess.plan == AccessPlan.Premium,
            onPreviewResume = { resume ->
                resumeDraftState.load(resume)
                navController.navigate(ResumeRoute.Preview)
            },
            onNavigateToNotifications = {
                navController.navigate(HomeRoute.Notifications) { launchSingleTop = true }
            },
            repository = resumeRepository,
            userProfile = authSessionViewModel.uiState,
            onNavigateToMyCvs = {
                navController.navigateToMainDestination(ResumeRoute.Graph)
            },
            onNavigateToSettings = {
                navController.navigateToMainDestination(SettingsRoute.Graph)
            },
            onNavigateToTemplates = {
                navController.navigate(HomeRoute.Templates)
            },
            onPreviewTemplate = { templateId ->
                navController.navigate(HomeRoute.TemplatePreview(templateId))
            },
            onNavigateBack = {
                navController.popBackStack()
            },
            onContinueResume = { resume ->
                resumeDraftState.load(resume)
                navController.navigate(ResumeRoute.SectionSelection)
            },
        )
        composable<HomeRoute.Notifications> {
            val entries by notificationHistory.entries.collectAsStateWithLifecycle()
            NotificationHistoryScreen(
                entries = entries,
                onBack = { navController.popBackStack() },
                onOpenMyCvs = { navController.navigateToMainDestination(ResumeRoute.Graph) },
            )
        }
        resumeGraph(
            userProfile = authSessionViewModel.uiState,
            isPremium = featureAccess.plan == AccessPlan.Premium,
            onNavigateToNotifications = {
                navController.navigate(HomeRoute.Notifications) { launchSingleTop = true }
            },
            onResumeSaved = onResumeSaved,
            navController = navController,
            repository = resumeRepository,
            draftState = resumeDraftState,
            importPolicy = resumeImportPolicy,
            onNavigateToHome = {
                navController.navigateToMainDestination(HomeRoute.Graph)
            },
            onNavigateToSettings = {
                navController.navigateToMainDestination(SettingsRoute.Graph)
            },
        )
        settingsGraph(
            versionName = BuildConfig.VERSION_NAME,
            isPremium = featureAccess.plan == AccessPlan.Premium,
            onNavigateToMyPlan = {
                navController.navigate(SettingsRoute.MyPlan) { launchSingleTop = true }
            },
            onNavigateToPrivacy = {
                navController.navigate(SettingsRoute.Privacy) {
                    launchSingleTop = true
                }
            },
            onNavigateToHelp = {
                navController.navigate(SettingsRoute.HelpCenter) {
                    launchSingleTop = true
                }
            },
            onNavigateBack = { navController.popBackStack() },
            autoSaveEnabled = autoSaveEnabled,
            onAutoSaveEnabledChange = onAutoSaveEnabledChange,
            notificationsEnabled = notificationsEnabled,
            onNotificationsEnabledChange = onNotificationsEnabledChange,
            appAppearance = appAppearance,
            onAppearanceSelected = onAppearanceSelected,
            appLanguage = appLanguage,
            onLanguageSelected = onLanguageSelected,
            userProfile = authSessionViewModel.uiState,
            onUpdateName = { name -> authSessionViewModel.updateUserName(name)?.let(resources::getString) },
            onNavigateToHome = {
                navController.navigateToMainDestination(HomeRoute.Graph)
            },
            onNavigateToMyCvs = {
                navController.navigateToMainDestination(ResumeRoute.Graph)
            },
            onLogout = { showLogoutConfirmation = true },
        )
    }

    MainScreenBackHandler(currentBackStackEntry?.destination) {
        showLogoutConfirmation = true
    }
    if (showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutConfirmation = false },
            onConfirm = {
                showLogoutConfirmation = false
                resumeDraftState.startNew(emptySet())
                authSessionViewModel.logout()
                navController.navigate(AuthRoute.Graph) {
                    popUpTo(navController.graph.id) { inclusive = false }
                    launchSingleTop = true
                }
                navController.clearBackStack<HomeRoute.Graph>()
                navController.clearBackStack<ResumeRoute.Graph>()
                navController.clearBackStack<SettingsRoute.Graph>()
            },
        )
    }

    LaunchedEffect(openMyCvsRequested, currentBackStackEntry) {
        val destination = currentBackStackEntry?.destination ?: return@LaunchedEffect
        val isMainDestination = destination.hierarchy.any {
            it.hasRoute<HomeRoute.Graph>() || it.hasRoute<ResumeRoute.Graph>() ||
                it.hasRoute<SettingsRoute.Graph>() || it.hasRoute<HomeRoute.Notifications>()
        }
        if (openMyCvsRequested && isMainDestination && authSessionViewModel.isAuthenticated()) {
            navController.navigate(ResumeRoute.Graph) {
                popUpTo<HomeRoute.Graph>()
                launchSingleTop = true
            }
            onOpenMyCvsHandled()
        }
    }
}

private fun NavHostController.navigateToMainDestination(route: Any) {
    navigate(route) {
        popUpTo<HomeRoute.Graph> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
