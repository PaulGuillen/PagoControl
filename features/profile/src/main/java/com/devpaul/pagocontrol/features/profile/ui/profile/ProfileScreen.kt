package com.devpaul.pagocontrol.features.profile.ui.profile

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.designsystem.PagoDock
import com.devpaul.pagocontrol.designsystem.PagoHeader
import com.devpaul.pagocontrol.features.profile.R

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    themeIndex: Int,
    version: String,
    onThemeSelected: (Int) -> Unit,
    onIntent: (ProfileUiIntent) -> Unit,
    onHome: () -> Unit,
    onMovements: () -> Unit,
    onHelp: () -> Unit,
    onPrivacy: () -> Unit,
    onReports: () -> Unit = {},
) {
    val context = LocalContext.current
    val preview = LocalInspectionMode.current
    fun notificationsAllowed(): Boolean {
        if (preview) return true
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                        manager.getNotificationChannel("recorded_movements")?.importance != NotificationManager.IMPORTANCE_NONE)
    }

    var systemNotificationsEnabled by remember { mutableStateOf(notificationsAllowed()) }
    LifecycleResumeEffect(context) {
        systemNotificationsEnabled = notificationsAllowed()
        onPauseOrDispose { }
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            systemNotificationsEnabled = notificationsAllowed()
            onIntent(ProfileUiIntent.Notifications(granted))
        }
    val toggleNotifications: (Boolean) -> Unit = { enabled ->
        when {
            !enabled -> onIntent(ProfileUiIntent.Notifications(false))
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ->
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            !notificationsAllowed() -> context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    context.packageName
                )
            )

            else -> onIntent(ProfileUiIntent.Notifications(true))
        }
    }
    val themeLabel = stringResource(
        listOf(
            R.string.theme_system,
            R.string.theme_light,
            R.string.theme_dark
        ).getOrElse(themeIndex) { R.string.theme_system })
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PagoDock(
                false,
                onHome,
                onMovements,
                profileSelected = true,
                onProfile = {},
                onReports = onReports
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { PagoHeader(stringResource(R.string.profile_title)) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.profile_heading),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            stringResource(R.string.profile_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (state.loading) item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else {
                    item { IdentityCard(state) { onIntent(ProfileUiIntent.Open(ProfileDialog.Edit)) } }
                    item { SectionLabel(stringResource(R.string.preferences)) }
                    item {
                        SettingsGroup {
                            SettingRow(
                                Icons.Outlined.Language,
                                stringResource(R.string.language),
                                stringResource(R.string.language_detail),
                                stringResource(R.string.spanish)
                            ) { onIntent(ProfileUiIntent.Open(ProfileDialog.Language)) }
                            SettingsDivider()
                            SettingRow(
                                Icons.Outlined.DarkMode,
                                stringResource(R.string.appearance),
                                stringResource(R.string.appearance_detail),
                                themeLabel
                            ) { onIntent(ProfileUiIntent.Open(ProfileDialog.Appearance)) }
                            SettingsDivider()
                            SettingRow(
                                Icons.Outlined.Notifications,
                                stringResource(R.string.notifications),
                                stringResource(if (systemNotificationsEnabled) R.string.notifications_detail else R.string.notifications_permission_detail),
                                enabled = !state.saving,
                                trailing = {
                                    Switch(
                                        checked = state.notificationsEnabled && systemNotificationsEnabled,
                                        onCheckedChange = toggleNotifications,
                                        enabled = !state.saving
                                    )
                                },
                            ) { toggleNotifications(!(state.notificationsEnabled && systemNotificationsEnabled)) }
                            if (!systemNotificationsEnabled) {
                                TextButton(onClick = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(
                                                Settings.EXTRA_APP_PACKAGE,
                                                context.packageName
                                            )
                                    )
                                }) {
                                    Text(stringResource(R.string.notifications_open_settings))
                                }
                            }
                        }
                    }
                    item { SectionLabel(stringResource(R.string.account_support)) }
                    item {
                        SettingsGroup {
                            SettingRow(
                                Icons.AutoMirrored.Outlined.HelpOutline,
                                stringResource(R.string.help),
                                stringResource(R.string.help_detail),
                                onClick = onHelp
                            )
                            SettingsDivider()
                            SettingRow(
                                Icons.Outlined.StarBorder,
                                stringResource(R.string.rate),
                                stringResource(R.string.rate_detail)
                            ) { onIntent(ProfileUiIntent.Open(ProfileDialog.Rating)) }
                            SettingsDivider()
                            SettingRow(
                                Icons.Outlined.Shield,
                                stringResource(R.string.privacy),
                                stringResource(R.string.privacy_detail),
                                onClick = onPrivacy
                            )
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = { onIntent(ProfileUiIntent.Open(ProfileDialog.SignOut)) },
                            enabled = !state.saving,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(18.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.sign_out))
                        }
                    }
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.version, version),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.local_demo),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.notice == ProfileNotice.LoadError) TextButton(onClick = {
                                onIntent(
                                    ProfileUiIntent.Retry
                                )
                            }) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
            }
        }
    }
    ProfileDialogs(state, themeIndex, onThemeSelected, onIntent)
}

@Composable
private fun IdentityCard(state: ProfileUiState, onEdit: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    Text(
                        state.name.split(" ").filter { it.isNotBlank() }.take(2)
                            .joinToString("") { it.take(1).uppercase() },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    state.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    state.email.ifBlank { stringResource(R.string.no_email) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onEdit,
                enabled = !state.saving,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) { Text(stringResource(R.string.edit)) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        Modifier.padding(start = 4.dp, top = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { content() }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        Modifier.padding(start = 84.dp, end = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    detail: String,
    value: String? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, enabled = enabled, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f)
            ) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) trailing() else {
                if (value != null) Text(
                    value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    null,
                    Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Perfil claro", showBackground = true)
@Composable
private fun ProfileLightPreview() {
    PagoControlTheme(false) {
        ProfileScreen(
            ProfileUiState(
                name = "Carlos Mendoza",
                email = "carlos@ejemplo.com",
                loading = false
            ), 1, "1.0", {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Perfil oscuro", showBackground = true)
@Composable
private fun ProfileDarkPreview() {
    PagoControlTheme(true) {
        ProfileScreen(
            ProfileUiState(
                name = "Carlos Mendoza",
                email = "carlos@ejemplo.com",
                loading = false
            ), 2, "1.0", {}, {}, {}, {}, {}, {})
    }
}
