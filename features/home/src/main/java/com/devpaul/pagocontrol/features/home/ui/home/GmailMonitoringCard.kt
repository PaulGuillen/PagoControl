package com.devpaul.pagocontrol.features.home.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.domain.GmailTransactionCandidate
import com.devpaul.pagocontrol.features.home.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun GmailMonitoringCard(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    access: Boolean,
    switchModifier: Modifier = Modifier,
    onSwitchEnabled: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    var installed by remember { mutableStateOf<Boolean?>(null) }
    var icon by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(context) {
        val result = withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getApplicationInfo(GmailTransactionCandidate.PACKAGE_NAME, 0) }.getOrNull()?.let { info ->
                runCatching { context.packageManager.getApplicationIcon(info).toBitmap(width = 96, height = 96).asImageBitmap() }.getOrNull() to info.enabled
            }
        }
        installed = result?.second ?: false
        icon = result?.first
    }
    val status = when {
        installed == false -> R.string.gmail_not_installed
        state.gmailMonitoring && access -> R.string.gmail_monitoring
        state.gmailMonitoring -> R.string.gmail_permission_pending
        else -> R.string.gmail_paused
    }
    val switchEnabled = installed == true && !state.savingNotification
    LaunchedEffect(switchEnabled) { onSwitchEnabled(switchEnabled) }
    PagoCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val gmailIcon = icon
            if (gmailIcon != null) Image(bitmap = gmailIcon, contentDescription = null, modifier = Modifier.size(48.dp))
            else Icon(imageVector = Icons.Outlined.MailOutline, contentDescription = null, modifier = Modifier.size(48.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.gmail_name), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(status), style = MaterialTheme.typography.bodySmall)
            }
            val monitoringLabel = stringResource(R.string.gmail_switch)
            Switch(
                checked = state.gmailMonitoring,
                onCheckedChange = { onIntent(HomeUiIntent.SetGmailMonitoring(it)) },
                enabled = switchEnabled,
                modifier = switchModifier.semantics { contentDescription = monitoringLabel },
            )
        }
        Text(stringResource(R.string.gmail_capture_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.gmailPendingNotifications.isNotEmpty()) Text(stringResource(R.string.gmail_pending_count, state.gmailPendingNotifications.size))
        if (state.gmailError) {
            Text(stringResource(R.string.notifications_error), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { onIntent(HomeUiIntent.Retry) }) { Text(stringResource(R.string.home_retry)) }
        }
    }
}
