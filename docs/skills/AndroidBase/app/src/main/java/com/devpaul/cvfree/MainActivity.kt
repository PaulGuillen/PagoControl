package com.devpaul.cvfree

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.AppAppearance
import com.devpaul.core.platform.theme.AppAppearanceManager
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.locale.AppLanguageManager
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.preferences.ResumePreferences
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.runtime.LaunchedEffect
import com.devpaul.cvfree.notifications.ResumeNotificationManager
import com.devpaul.cvfree.ui.ForceUpdateScreen
import com.devpaul.cvfree.ui.UpdateState
import com.devpaul.cvfree.ui.openPlayStore
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguageManager.wrap(newBase))
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var resumeNotifications: ResumeNotificationManager

    private val immediateUpdateOptions =
        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()

    private var updateState by mutableStateOf<UpdateState>(UpdateState.Loading)
    private var appLanguage by mutableStateOf(AppLanguage.Spanish)
    private var appAppearance by mutableStateOf(AppAppearance.System)
    private var notificationsEnabled by mutableStateOf(false)
    private var autoSaveEnabled by mutableStateOf(true)
    private var openMyCvsRequested by mutableStateOf(false)
    private var pendingSaveNotification = false

    private val notificationPermissionLauncher = registerForActivityResult(RequestPermission()) { granted ->
        AppAnalytics.outcome("notification_permission", if (granted) "granted" else "denied")
        resumeNotifications.enabled = granted
        refreshNotificationState()
        if (granted && pendingSaveNotification) resumeNotifications.showResumeSaved()
        pendingSaveNotification = false
    }

    private val immediateUpdateLauncher =
        registerForActivityResult(StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                showForceUpdateMandatoryMessageAndClose()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appLanguage = AppLanguageManager.current(this)
        appAppearance = AppAppearanceManager.current(this)
        autoSaveEnabled = ResumePreferences.autoSaveEnabled(this)
        resumeNotifications = ResumeNotificationManager(applicationContext)
        resumeNotifications.ensureChannel()
        refreshNotificationState()
        pendingSaveNotification = savedInstanceState?.getBoolean(PENDING_SAVE_NOTIFICATION) ?: false
        openMyCvsRequested = savedInstanceState?.getBoolean(OPEN_MY_CVS_REQUESTED)
            ?: intent.getBooleanExtra(ResumeNotificationManager.EXTRA_OPEN_MY_CVS, false)
        enableEdgeToEdge()

        validateAppUpdate()

        setContent {
            LaunchedEffect(updateState) {
                if (updateState == UpdateState.Required) AppAnalytics.screenView("force_update")
            }
            val isDarkTheme = appAppearance.isDarkTheme(isSystemInDarkTheme())
            val statusBarColor = if (isDarkTheme) Color.Black else Color.White
            val navController = rememberNavController()
            val localizedContext = remember(appLanguage) {
                AppLanguageManager.wrap(this@MainActivity, appLanguage)
            }

            SetStatusBarColor(color = statusBarColor, darkIcons = !isDarkTheme)

            key(appLanguage) {
                CompositionLocalProvider(LocalResources provides localizedContext.resources) {
                    CvFreeTheme(darkTheme = isDarkTheme) {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            when (updateState) {
                                UpdateState.Required -> {
                                    ForceUpdateScreen(onOpenStore = ::startImmediateUpdate)
                                }

                                UpdateState.NotRequired -> {
                                    MainGraph(
                                        navController = navController,
                                        appLanguage = appLanguage,
                                        appAppearance = appAppearance,
                                        notificationsEnabled = notificationsEnabled,
                                        autoSaveEnabled = autoSaveEnabled,
                                        onAutoSaveEnabledChange = { enabled ->
                                            AppAnalytics.selection("auto_save", enabled.toString())
                                            ResumePreferences.setAutoSaveEnabled(this@MainActivity, enabled)
                                            autoSaveEnabled = enabled
                                        },
                                        onNotificationsEnabledChange = ::onNotificationsEnabledChange,
                                        onResumeSaved = ::onResumeSaved,
                                        notificationHistory = resumeNotifications.history,
                                        openMyCvsRequested = openMyCvsRequested,
                                        onOpenMyCvsHandled = {
                                            openMyCvsRequested = false
                                            intent.removeExtra(ResumeNotificationManager.EXTRA_OPEN_MY_CVS)
                                        },
                                        onAppearanceSelected = { appearance ->
                                            AppAnalytics.selection("app_appearance", appearance.name.lowercase())
                                            AppAppearanceManager.set(this@MainActivity, appearance)
                                            appAppearance = appearance
                                        },
                                        onLanguageSelected = { language ->
                                            AppAnalytics.selection("app_language", language.name.lowercase())
                                            AppLanguageManager.set(this@MainActivity, language)
                                            appLanguage = language
                                            resumeNotifications.ensureChannel()
                                        },
                                    )
                                }

                                UpdateState.Loading -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshNotificationState()
        validateAppUpdate()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(ResumeNotificationManager.EXTRA_OPEN_MY_CVS, false)) {
            openMyCvsRequested = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(PENDING_SAVE_NOTIFICATION, pendingSaveNotification)
        outState.putBoolean(OPEN_MY_CVS_REQUESTED, openMyCvsRequested)
        super.onSaveInstanceState(outState)
    }

    private fun refreshNotificationState() {
        notificationsEnabled = resumeNotifications.enabled && resumeNotifications.canPostNotifications()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resumeNotifications.permissionRequested = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun onNotificationsEnabledChange(enabled: Boolean) {
        AppAnalytics.selection("notifications_enabled", enabled.toString())
        resumeNotifications.enabled = enabled
        if (enabled) {
            if (!resumeNotifications.hasPermission() &&
                (!resumeNotifications.permissionRequested ||
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
            ) {
                requestNotificationPermission()
            } else if (!resumeNotifications.canPostNotifications()) {
                startActivity(resumeNotifications.settingsIntent())
            }
        }
        refreshNotificationState()
    }

    private fun onResumeSaved() {
        resumeNotifications.history.recordResumeSaved()
        if (!resumeNotifications.enabled) return
        if (resumeNotifications.canPostNotifications()) {
            resumeNotifications.showResumeSaved()
        } else if (!resumeNotifications.hasPermission() && !resumeNotifications.permissionRequested) {
            pendingSaveNotification = true
            requestNotificationPermission()
        }
    }

    private fun validateAppUpdate() {
        lifecycleScope.launch {
            updateState = checkForAppUpdate()
        }
    }

    private suspend fun checkForAppUpdate(): UpdateState {
        return try {
            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()

            val updateInProgress =
                appUpdateInfo.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

            val hasUpdate =
                appUpdateInfo.updateAvailability() ==
                        UpdateAvailability.UPDATE_AVAILABLE

            val immediateAllowed =
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

            when {
                updateInProgress -> UpdateState.Required
                hasUpdate && immediateAllowed -> UpdateState.Required
                else -> UpdateState.NotRequired
            }
        } catch (error: Exception) {
            AppAnalytics.error("check_app_update", error)
            UpdateState.NotRequired
        }
    }

    private fun startImmediateUpdate() {
        lifecycleScope.launch {
            try {
                val appUpdateInfo = appUpdateManager.appUpdateInfo.await()

                val started = appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    immediateUpdateLauncher,
                    immediateUpdateOptions
                )

                if (!started) {
                    openPlayStore(this@MainActivity)
                }
            } catch (error: Exception) {
                AppAnalytics.error("start_app_update", error)
                openPlayStore(this@MainActivity)
            }
        }
    }

    private fun showForceUpdateMandatoryMessageAndClose() {
        Toast.makeText(
            this,
            AppLanguageManager.wrap(this, appLanguage).getString(R.string.ui_you_must_update_the_app_to_continue_76be00),
            Toast.LENGTH_LONG
        ).show()

        finish()
    }

    private companion object {
        const val PENDING_SAVE_NOTIFICATION = "pending_save_notification"
        const val OPEN_MY_CVS_REQUESTED = "open_my_cvs_requested"
    }
}
