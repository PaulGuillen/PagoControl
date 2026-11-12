package com.devpaul.pagocontrol

import com.devpaul.pagocontrol.navigation.route.AuthRoute
import com.devpaul.pagocontrol.navigation.route.HomeRoute
import com.devpaul.pagocontrol.navigation.route.MovementsRoute
import com.devpaul.pagocontrol.navigation.route.ProfileRoute
import com.devpaul.pagocontrol.navigation.route.ReportsRoute
import com.devpaul.pagocontrol.features.reports.reportsGraph
import com.devpaul.pagocontrol.navigation.route.GraphRoute
import com.devpaul.pagocontrol.features.profile.profileGraph
import com.devpaul.pagocontrol.features.movements.movementsGraph
import com.devpaul.pagocontrol.navigation.route.SplashRoute
import com.devpaul.pagocontrol.navigation.route.OnboardingRoute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devpaul.pagocontrol.features.auth.ui.session.AuthSessionViewModel
import com.devpaul.pagocontrol.features.auth.domain.usecase.SignOutUseCase
import com.devpaul.pagocontrol.ui.LogoutConfirmationDialog
import com.devpaul.pagocontrol.ui.MainScreenBackHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import android.widget.Toast
import com.devpaul.pagocontrol.features.auth.authGraph
import com.devpaul.pagocontrol.features.home.homeGraph
import com.devpaul.pagocontrol.features.splash.splashGraph
import com.devpaul.pagocontrol.features.onboarding.onboardingGraph

@Composable
fun MainGraph(
    themeIndex: Int,
    onThemeSelected: (Int) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val authSession: AuthSessionViewModel = koinViewModel()
    val signOut: SignOutUseCase = koinInject()
    val session by authSession.session.collectAsStateWithLifecycle()
    val authenticated = session != null
    val userName = session?.name.orEmpty()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }
    var signingOut by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner, authSession) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) authSession.refreshUser()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(session?.id) { authSession.refreshUser() }
    val controller = navController
    val context = LocalContext.current
    val version = remember(context) {
        context.packageManager.getPackageInfo(
            context.packageName,
            0
        ).versionName.orEmpty()
    }

    fun navigateTab(route: GraphRoute) {
        controller.navigate(route) {
            popUpTo<HomeRoute.Home> { saveState = true }
            restoreState = true
            launchSingleTop = true
        }
    }

    var onboardingCompleted by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val currentEntry by controller.currentBackStackEntryAsState()
    LaunchedEffect(currentEntry?.destination) {
        showLogoutConfirmation = false
        val destination = currentEntry?.destination ?: return@LaunchedEffect
        val screen = when {
            destination.hasRoute<AuthRoute.Login>() -> "login"
            destination.hasRoute<AuthRoute.Register>() -> "register"
            destination.hasRoute<AuthRoute.ForgotPassword>() -> "password_reset"
            destination.hasRoute<HomeRoute.Home>() -> "home"
            destination.hasRoute<MovementsRoute.Movements>() -> "movements"
            destination.hasRoute<ReportsRoute.Reports>() -> "reports"
            destination.hasRoute<ProfileRoute.Profile>() -> "profile"
            destination.hasRoute<ProfileRoute.Help>() -> "help"
            destination.hasRoute<ProfileRoute.Privacy>() -> "privacy"
            destination.hasRoute<SplashRoute.Splash>() -> "splash"
            else -> "onboarding"
        }
        AppAnalytics.screenView(screen)
    }
    if (showLogoutConfirmation && session != null) {
        LogoutConfirmationDialog(
            loading = signingOut,
            onDismiss = { if (!signingOut) showLogoutConfirmation = false },
            onConfirm = {
                if (!signingOut) {
                    signingOut = true
                    scope.launch {
                        try {
                            signOut()
                            showLogoutConfirmation = false
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            AppAnalytics.error("sign_out", error)
                            Toast.makeText(context, R.string.logout_error, Toast.LENGTH_LONG).show()
                        } finally {
                            signingOut = false
                        }
                    }
                }
            },
        )
    }
    LaunchedEffect(
        key1 = session?.id,
        key2 = currentEntry?.destination,
        key3 = onboardingCompleted
    ) {
        val destination = currentEntry?.destination ?: return@LaunchedEffect
        val splash = destination.hierarchy.any { it.hasRoute<SplashRoute.Graph>() }
        val onboarding = destination.hierarchy.any { it.hasRoute<OnboardingRoute.Graph>() }
        if (splash) {
            val completed = onboardingCompleted ?: return@LaunchedEffect
            controller.navigate(if (session != null) HomeRoute.Graph else if (completed) AuthRoute.Graph else OnboardingRoute.Graph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!onboarding) {
            val correct =
                destination.hierarchy.any { if (session == null) it.hasRoute<AuthRoute.Graph>() else it.hasRoute<HomeRoute.Graph>() || it.hasRoute<MovementsRoute.Graph>() || it.hasRoute<ProfileRoute.Graph>() || it.hasRoute<ReportsRoute.Graph>() }
            if (!correct) {
                controller.clearBackStack<MovementsRoute.Graph>()
                controller.clearBackStack<ProfileRoute.Graph>()
                controller.clearBackStack<ReportsRoute.Graph>()
                controller.navigate(if (session == null) AuthRoute.Graph else HomeRoute.Graph) {
                    popUpTo(controller.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
    NavHost(navController = controller, startDestination = SplashRoute.Graph) {
        splashGraph { onboardingCompleted = it }
        onboardingGraph {
            controller.navigate(AuthRoute.Graph) {
                popUpTo(controller.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        authGraph(
            themeIndex = themeIndex, onThemeSelected = onThemeSelected,
            onRegister = { controller.navigate(AuthRoute.Register) { launchSingleTop = true } },
            onForgotPassword = {
                controller.navigate(AuthRoute.ForgotPassword) {
                    launchSingleTop = true
                }
            },
            onLogin = { controller.popBackStack<AuthRoute.Login>(inclusive = false) },
        )
        homeGraph(
            name = userName,
            themeIndex = themeIndex,
            onThemeSelected = onThemeSelected,
            authorized = authenticated,
            onMovements = { navigateTab(MovementsRoute.Graph) },
            onProfile = { navigateTab(ProfileRoute.Graph) },
            onReports = { navigateTab(ReportsRoute.Graph) },
        )
        movementsGraph(
            authorized = authenticated,
            onHome = { controller.popBackStack<HomeRoute.Home>(inclusive = false) },
            onProfile = { navigateTab(ProfileRoute.Graph) },
            onReports = { navigateTab(ReportsRoute.Graph) },
        )
        profileGraph(
            authorized = authenticated,
            themeIndex = themeIndex,
            version = version,
            onThemeSelected = onThemeSelected,
            onHome = { controller.popBackStack<HomeRoute.Home>(inclusive = false) },
            onMovements = { navigateTab(MovementsRoute.Graph) },
            onHelp = { controller.navigate(ProfileRoute.Help) { launchSingleTop = true } },
            onPrivacy = { controller.navigate(ProfileRoute.Privacy) { launchSingleTop = true } },
            onBack = { controller.popBackStack() },
            onReports = { navigateTab(ReportsRoute.Graph) },
        )
        reportsGraph(
            authorized = authenticated,
            onHome = { controller.popBackStack<HomeRoute.Home>(inclusive = false) },
            onMovements = { navigateTab(MovementsRoute.Graph) },
            onProfile = { navigateTab(ProfileRoute.Graph) },
        )
    }
    MainScreenBackHandler(
        destination = currentEntry?.destination.takeIf { session != null },
        onRequestLogout = { showLogoutConfirmation = true },
    )
}
