package com.devpaul.pagocontrol.features.home.ui.home

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.features.home.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class MonitoredApp(val packageName: String, val label: String, val icon: ImageBitmap? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationControls(state: HomeUiState, onIntent: (HomeUiIntent) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var showApps by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<MonitoredApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var appsFailed by remember { mutableStateOf(false) }
    var refreshToken by remember { mutableIntStateOf(0) }
    DisposableEffect(owner, context) {
        fun refresh() {
            refreshToken++
        }
        refresh()
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(refreshToken) {
        loading = true
        try {
            apps = withContext(Dispatchers.IO) {
                val manager = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                manager.queryIntentActivities(intent, 0)
                    .filter { it.activityInfo.packageName != context.packageName && it.activityInfo.packageName != "com.google.android.gm" }
                    .distinctBy { it.activityInfo.packageName }
                    .map { info ->
                        MonitoredApp(
                            packageName = info.activityInfo.packageName,
                            label = info.loadLabel(manager).toString(),
                            icon = runCatching { info.loadIcon(manager).toBitmap(width = 96, height = 96).asImageBitmap() }.getOrNull(),
                        )
                    }.sortedBy { it.label.lowercase() }
            }
            appsFailed = false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            appsFailed = true
        } finally {
            loading = false
        }
    }
    val selectedApps = state.notificationSources.sorted().map { packageName ->
        apps.find { it.packageName == packageName } ?: MonitoredApp(packageName = packageName, label = packageName.substringAfterLast('.'))
    }
    PagoCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { showApps = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.notifications_title),
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = selectedApps, key = { it.packageName }) { app ->
                Column(
                    modifier = Modifier.width(72.dp).clickable(role = Role.Button) { showApps = true }.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppIcon(app)
                    Text(text = app.label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            item {
                Column(
                    modifier = Modifier.width(72.dp).clickable(role = Role.Button) { showApps = true }.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.padding(12.dp).size(24.dp))
                    }
                    Text(stringResource(R.string.notifications_add_apps), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (state.pendingNotifications.isNotEmpty()) {
            Text(stringResource(R.string.notifications_pending_count, state.pendingNotifications.size), style = MaterialTheme.typography.labelMedium)
        }
        if (state.notificationError) {
            Text(stringResource(R.string.notifications_error), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { onIntent(HomeUiIntent.Retry) }) { Text(stringResource(R.string.home_retry)) }
        }
    }
    if (showApps) ModalBottomSheet(
        onDismissRequest = { showApps = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.notifications_choose_apps), style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.notifications_search_apps)) },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
            )
            val allApps = apps + selectedApps.filter { selected -> apps.none { it.packageName == selected.packageName } }
            val filteredApps = allApps.filter { it.label.contains(query.trim(), ignoreCase = true) }
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (loading) item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                if (appsFailed) item {
                    Text(stringResource(R.string.notifications_apps_error))
                    TextButton(onClick = { refreshToken++ }) { Text(stringResource(R.string.home_retry)) }
                }
                if (!loading && !appsFailed && filteredApps.isEmpty()) item { Text(stringResource(R.string.notifications_no_apps)) }
                items(items = filteredApps, key = { it.packageName }) { app ->
                    val selected = app.packageName in state.notificationSources
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
                            .toggleable(value = selected, enabled = !state.savingNotification, role = Role.Checkbox) {
                                onIntent(HomeUiIntent.SetNotificationSource(packageName = app.packageName, enabled = it))
                            }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AppIcon(app)
                        Text(text = app.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Checkbox(checked = selected, onCheckedChange = null, enabled = !state.savingNotification)
                    }
                }
            }
            Text(stringResource(R.string.notifications_gmail_separate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { showApps = false }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Text(stringResource(R.string.notifications_done))
            }
        }
    }
}

@Composable
private fun AppIcon(app: MonitoredApp) {
    if (app.icon != null) {
        Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(48.dp))
    } else {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Icon(imageVector = Icons.Outlined.Apps, contentDescription = null, modifier = Modifier.padding(12.dp).size(24.dp))
        }
    }
}
