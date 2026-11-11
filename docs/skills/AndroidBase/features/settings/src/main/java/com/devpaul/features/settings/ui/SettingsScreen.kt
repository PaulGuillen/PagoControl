package com.devpaul.features.settings.ui

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.analytics.TrackAnalyticsView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.core.designsystem.component.CvBottomNavigation
import com.devpaul.core.designsystem.component.CvAppHeader
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.core.designsystem.component.CvMainDestination
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.theme.AppAppearance
import com.devpaul.features.settings.ui.components.AppearanceSelectionDialog
import com.devpaul.features.settings.ui.util.subtitleRes
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.settings.R
import com.devpaul.features.settings.ui.components.EditNameDialog
import com.devpaul.features.settings.ui.components.LanguageSelectionDialog
import com.devpaul.features.settings.ui.components.ProfileCard
import com.devpaul.features.settings.ui.components.SettingsDivider
import com.devpaul.features.settings.ui.components.SettingsLinkRow
import com.devpaul.features.settings.ui.components.SettingsSectionTitle
import com.devpaul.features.settings.ui.components.SettingsSwitchRow
import com.devpaul.features.settings.ui.util.MAX_PROFILE_NAME_LENGTH
import com.devpaul.features.settings.ui.util.isValidProfileName
import com.devpaul.features.settings.ui.util.labelRes
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToMyCvs: () -> Unit,
    onLogout: () -> Unit,
    userName: String,
    userEmail: String,
    onUpdateName: suspend (String) -> String?,
    appLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    appAppearance: AppAppearance,
    onAppearanceSelected: (AppAppearance) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    autoSaveEnabled: Boolean,
    onAutoSaveEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToHelp: () -> Unit = {},
    onRateApp: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    isPremium: Boolean = false,
    onNavigateToMyPlan: () -> Unit = {},
    isProfileLoading: Boolean = false,
    versionName: String = "",
) {
    var isEditingName by rememberSaveable { mutableStateOf(false) }
    var editedName by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var isSavingName by rememberSaveable { mutableStateOf(false) }
    var isChoosingLanguage by rememberSaveable { mutableStateOf(false) }
    var isChoosingAppearance by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nameUpdatedMessage = stringResource(R.string.settings_name_updated)
    val updateNameErrorMessage = stringResource(R.string.settings_update_name_error)
    val invalidNameMessage = stringResource(R.string.settings_invalid_name)
    val background = MaterialTheme.colorScheme.background

    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Scaffold(
        modifier = modifier,
        containerColor = background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CvBottomNavigation(
                selectedDestination = CvMainDestination.Settings,
                onDestinationSelected = { destination ->
                    when (destination) {
                        CvMainDestination.Home -> onNavigateToHome()
                        CvMainDestination.MyCvs -> onNavigateToMyCvs()
                        CvMainDestination.Settings -> Unit
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                CvAppHeader(
                    profile = UserProfileUiState(isLoading = isProfileLoading, name = userName, email = userEmail),
                    isPremium = isPremium,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            item {
                ProfileCard(
                    userName = userName,
                    userEmail = userEmail,
                    isPremium = isPremium,
                    onEdit = {
                        editedName = userName
                        nameError = null
                        isEditingName = true
                    },
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        SettingsLinkRow(
                            icon = Icons.Outlined.WorkspacePremium,
                            title = stringResource(R.string.settings_my_plan),
                            subtitle = stringResource(R.string.settings_my_plan_subtitle),
                            value = stringResource(if (isPremium) R.string.settings_plan_premium else R.string.settings_plan_free),
                            onClick = trackedClick("open_my_plan", onNavigateToMyPlan),
                        )
                    }
                }
            }
            item {
                SettingsSectionTitle(text = stringResource(R.string.settings_preferences))
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        SettingsLinkRow(
                            icon = Icons.Outlined.Language,
                            title = stringResource(R.string.settings_language),
                            subtitle = stringResource(R.string.settings_language_subtitle),
                            value = stringResource(appLanguage.labelRes()),
                            onClick = trackedClick("open_language_dialog") { isChoosingLanguage = true },
                        )
                        SettingsDivider()
                        SettingsLinkRow(
                            icon = Icons.Outlined.DarkMode,
                            title = stringResource(R.string.settings_appearance),
                            subtitle = stringResource(appAppearance.subtitleRes()),
                            value = stringResource(appAppearance.labelRes()),
                            onClick = trackedClick("open_appearance_dialog") { isChoosingAppearance = true },
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Notifications,
                            title = stringResource(R.string.settings_notifications),
                            subtitle = stringResource(R.string.settings_notifications_subtitle),
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationsEnabledChange,
                        )
                        SettingsDivider()
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Done,
                            title = stringResource(R.string.settings_autosave),
                            subtitle = stringResource(R.string.settings_autosave_subtitle),
                            checked = autoSaveEnabled,
                            onCheckedChange = onAutoSaveEnabledChange,
                        )
                    }
                }
            }
            item {
                SettingsSectionTitle(text = stringResource(R.string.settings_account_support))
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        SettingsLinkRow(
                            icon = Icons.AutoMirrored.Outlined.HelpOutline,
                            title = stringResource(R.string.settings_help),
                            subtitle = stringResource(R.string.settings_help_subtitle),
                            onClick = trackedClick("open_help", onNavigateToHelp),
                        )
                        SettingsDivider()
                        SettingsLinkRow(
                            icon = Icons.Outlined.StarOutline,
                            title = stringResource(R.string.settings_rate),
                            subtitle = stringResource(R.string.settings_rate_subtitle),
                            onClick = trackedClick("open_rating_dialog", onRateApp),
                        )
                        SettingsDivider()
                        SettingsLinkRow(
                            icon = Icons.Outlined.PrivacyTip,
                            title = stringResource(R.string.settings_privacy),
                            subtitle = stringResource(R.string.settings_privacy_subtitle),
                            onClick = trackedClick("open_privacy", onNavigateToPrivacy),
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = trackedClick("logout", onLogout),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_logout), style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.settings_version, versionName),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }

    if (isEditingName) {
        TrackAnalyticsView("edit_profile_name_dialog")
        EditNameDialog(
            name = editedName,
            errorMessage = nameError,
            isSaving = isSavingName,
            onNameChange = {
                editedName = it.take(MAX_PROFILE_NAME_LENGTH)
                nameError = null
            },
            onDismiss = { isEditingName = false },
            onSave = {
                val normalizedName = editedName.trim()
                if (!isValidProfileName(normalizedName)) {
                    AppAnalytics.error("update_profile_name", code = "invalid_name")
                    nameError = invalidNameMessage
                } else {
                    scope.launch {
                        isSavingName = true
                        val error = onUpdateName(normalizedName)
                        isSavingName = false
                        if (error == null) {
                            isEditingName = false
                            snackbarHostState.showSnackbar(nameUpdatedMessage)
                        } else {
                            nameError = updateNameErrorMessage
                        }
                    }
                }
            },
        )
    }

    if (isChoosingAppearance) {
        TrackAnalyticsView("appearance_dialog")
        AppearanceSelectionDialog(
            selectedAppearance = appAppearance,
            onDismiss = { isChoosingAppearance = false },
            onAppearanceSelected = { appearance ->
                isChoosingAppearance = false
                if (appearance != appAppearance) {
                    onAppearanceSelected(appearance)
                }
            },
        )
    }

    if (isChoosingLanguage) {
        TrackAnalyticsView("language_dialog")
        LanguageSelectionDialog(
            selectedLanguage = appLanguage,
            onDismiss = { isChoosingLanguage = false },
            onLanguageSelected = { language ->
                isChoosingLanguage = false
                if (language != appLanguage) {
                    onLanguageSelected(language)
                }
            },
        )
    }
}
