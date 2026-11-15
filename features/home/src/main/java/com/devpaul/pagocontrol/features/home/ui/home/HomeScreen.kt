package com.devpaul.pagocontrol.features.home.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.designsystem.PagoDock
import com.devpaul.pagocontrol.designsystem.PagoHeader
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentSyncState
import com.devpaul.pagocontrol.features.home.ui.home.components.AllMovementsScreen
import com.devpaul.pagocontrol.features.home.ui.home.components.FlowCard
import com.devpaul.pagocontrol.features.home.ui.home.components.GmailMonitoringCard
import com.devpaul.pagocontrol.features.home.ui.home.components.HomeGuideOverlay
import com.devpaul.pagocontrol.features.home.ui.home.components.HomeSectionTabs
import com.devpaul.pagocontrol.features.home.ui.home.components.MailBalanceCard
import com.devpaul.pagocontrol.features.home.ui.home.components.MailReviewTitle
import com.devpaul.pagocontrol.features.home.ui.home.components.MailSummaryCard
import com.devpaul.pagocontrol.features.home.ui.home.components.MovementRow
import com.devpaul.pagocontrol.features.home.ui.home.components.NotificationAccessCard
import com.devpaul.pagocontrol.features.home.ui.home.components.NotificationControls
import com.devpaul.pagocontrol.features.home.ui.home.components.ReceivedPaymentsCard
import com.devpaul.pagocontrol.features.home.ui.home.components.model.HomeGuideAction
import com.devpaul.pagocontrol.features.home.ui.home.model.HomeContent
import com.devpaul.pagocontrol.features.home.ui.home.model.HomeSection
import com.devpaul.pagocontrol.features.home.ui.home.util.rememberNotificationAccess
import com.devpaul.pagocontrol.features.home.ui.notificationreview.NotificationReviewScreen

@Composable
fun HomeScreen(
    state: HomeUiState,
    name: String,
    onIntent: (HomeUiIntent) -> Unit,
    themeIndex: Int,
    onThemeSelected: (Int) -> Unit,
    onMovements: () -> Unit = {},
    onProfile: () -> Unit = {},
    onReports: () -> Unit = {},
    notificationAccessGranted: Boolean = rememberNotificationAccess(),
    syncState: PaymentSyncState = PaymentSyncState(),
    onSync: () -> Unit = {},
) {
    val context = LocalContext.current
    var settingsFailed by remember { mutableStateOf(false) }
    val activateAccess: () -> Unit = {
        settingsFailed =
            runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }.isFailure
    }
    var previousAccess by remember { mutableStateOf(notificationAccessGranted) }
    LaunchedEffect(notificationAccessGranted) {
        if (notificationAccessGranted && !previousAccess && state.guideStep == 0) onIntent(
            HomeUiIntent.NextGuideStep
        )
        previousAccess = notificationAccessGranted
    }
    val mail = state.section == HomeSection.Mail
    val listState = rememberLazyListState()
    val ready = (if (mail) state.gmailContent else state.content) is HomeContent.Ready
    var showAllMovements by rememberSaveable(state.section) { mutableStateOf(false) }
    var showMovementReview by rememberSaveable(state.section) { mutableStateOf(false) }
    var reviewedIds by rememberSaveable(state.section) { mutableStateOf(arrayListOf<String>()) }
    val pendingNotifications =
        if (mail) state.gmailPendingNotifications else state.pendingNotifications
    val pendingIds = pendingNotifications.map { it.id }
    val hasNewMovement = pendingIds.any { it !in reviewedIds }
    LaunchedEffect(pendingIds, showMovementReview) {
        if (showMovementReview) reviewedIds = ArrayList(pendingIds)
    }
    val reviewCount = pendingNotifications.size
    val pendingCount = if (reviewCount > 0) 1 else 0
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingTitle by rememberSaveable { mutableStateOf("") }
    val guideTargets =
        remember(state.section, notificationAccessGranted) { mutableStateMapOf<Int, Rect>() }
    val actionTargets =
        remember(state.section, notificationAccessGranted) { mutableStateMapOf<String, Rect>() }
    val accessButtonRequester = remember { BringIntoViewRequester() }
    var showApps by rememberSaveable { mutableStateOf(false) }
    var gmailSwitchEnabled by remember(state.section) { mutableStateOf(false) }
    val guiding = ready && state.guideStep != null && !showApps
    val guideAction = when {
        state.guideStep == 0 && !notificationAccessGranted -> actionTargets["access"]?.let {
            HomeGuideAction(
                bounds = it,
                label = stringResource(R.string.notifications_access),
                onClick = activateAccess
            )
        }

        state.guideStep == 1 && mail -> actionTargets["gmail"]?.let {
            HomeGuideAction(
                bounds = it,
                label = stringResource(R.string.gmail_switch),
                enabled = gmailSwitchEnabled,
                checked = state.gmailMonitoring,
                onClick = { onIntent(HomeUiIntent.SetGmailMonitoring(!state.gmailMonitoring)) },
            )
        }

        state.guideStep == 1 -> actionTargets["wallet"]?.let {
            HomeGuideAction(
                bounds = it,
                label = stringResource(R.string.notifications_add_apps),
                onClick = { showApps = true })
        }

        else -> null
    }
    LaunchedEffect(state.section, state.guideStep, ready, notificationAccessGranted) {
        if (ready && state.guideStep != null) {
            val index = when (state.guideStep) {
                0 -> if (notificationAccessGranted) 3 else 2
                1 -> (if (mail) 5 else 4) + if (notificationAccessGranted) 0 else 1
                2 -> (if (mail) 6 else 5) + pendingCount + (if (notificationAccessGranted) 0 else 1)
                else -> (if (mail) 7 else 6) + pendingCount + (if (notificationAccessGranted) 0 else 1)
            }
            listState.scrollToItem(index)
            if (state.guideStep == 0 && !notificationAccessGranted) {
                withFrameNanos { }
                accessButtonRequester.bringIntoView()
            }
        } else if (state.guideStep == null) {
            listState.scrollToItem(0)
        }
    }
    if (showMovementReview) {
        NotificationReviewScreen(
            notifications = pendingNotifications,
            saving = state.savingNotification,
            error = if (mail) state.gmailError else state.notificationError,
            mail = mail,
            onIntent = onIntent,
            onBack = { showMovementReview = false },
        )
        return
    }
    if (showAllMovements) {
        val content = (if (mail) state.gmailContent else state.content) as? HomeContent.Ready
        AllMovementsScreen(
            movements = content?.dashboard?.let {
                if (mail) it.movements else it.receivedMovements
            }.orEmpty(),
            mail = mail,
            saving = state.savingNotification,
            onDelete = { movement ->
                deletingId = movement.id
                deletingTitle = movement.title
            },
            onBack = { showAllMovements = false },
        )
    } else Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (guiding) Modifier.clearAndSetSemantics {} else Modifier,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                PagoDock(
                    false,
                    {},
                    onMovements,
                    onProfile = onProfile,
                    onReports = onReports
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        PagoHeader(stringResource(R.string.home_title))
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                stringResource(R.string.home_greeting, name),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = stringResource(
                                    when {
                                        syncState.hasError -> R.string.home_sync_error
                                        syncState.isSyncing && !syncState.initialDownloadComplete -> R.string.home_sync_initial
                                        syncState.isSyncing -> R.string.home_sync_active
                                        syncState.pendingWrites > 0 -> R.string.home_sync_pending
                                        syncState.initialDownloadComplete -> R.string.home_sync_complete
                                        else -> R.string.home_sync_local
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { onIntent(HomeUiIntent.ShowGuide) }) {
                                Text(stringResource(R.string.home_guide_help))
                            }
                            if (syncState.hasError) {
                                TextButton(onClick = onSync, enabled = !syncState.isSyncing) {
                                    Text(stringResource(R.string.home_sync_action))
                                }
                            }
                        }
                    }
                    if (!notificationAccessGranted) {
                        item(key = "notification-access") {
                            Column(Modifier.onGloballyPositioned {
                                guideTargets[0] = it.boundsInRoot()
                            }) {
                                NotificationAccessCard(
                                    settingsFailed = settingsFailed,
                                    onActivate = activateAccess,
                                    actionModifier = Modifier.bringIntoViewRequester(
                                        accessButtonRequester
                                    )
                                        .onGloballyPositioned {
                                            actionTargets["access"] = it.boundsInRoot()
                                        },
                                )
                            }
                        }
                    }
                    item { HomeSectionTabs(section = state.section, onIntent = onIntent) }
                    when (val content = if (mail) state.gmailContent else state.content) {
                        HomeContent.Loading -> item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        HomeContent.Error -> item {
                            PagoCard(Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.home_error))
                                Button(onClick = { onIntent(HomeUiIntent.Retry) }) {
                                    Text(
                                        stringResource(R.string.home_retry)
                                    )
                                }
                            }
                        }

                        is HomeContent.Ready -> {
                            val dashboard = content.dashboard
                            if (mail) {
                                item {
                                    Column(Modifier.onGloballyPositioned {
                                        if (notificationAccessGranted) guideTargets[0] =
                                            it.boundsInRoot()
                                    }) {
                                        MailBalanceCard(dashboard)
                                    }
                                }
                                item {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        MailSummaryCard(
                                            income = true,
                                            dashboard = dashboard,
                                            modifier = Modifier.weight(1f)
                                        )
                                        MailSummaryCard(
                                            income = false,
                                            dashboard = dashboard,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                item {
                                    Column(Modifier.onGloballyPositioned {
                                        guideTargets[1] = it.boundsInRoot()
                                    }) {
                                        GmailMonitoringCard(
                                            state = state,
                                            onIntent = onIntent,
                                            access = notificationAccessGranted,
                                            switchModifier = Modifier.onGloballyPositioned {
                                                actionTargets["gmail"] = it.boundsInRoot()
                                            },
                                            onSwitchEnabled = { gmailSwitchEnabled = it },
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Column(Modifier.onGloballyPositioned {
                                        if (notificationAccessGranted) guideTargets[0] =
                                            it.boundsInRoot()
                                    }) {
                                        ReceivedPaymentsCard(dashboard)
                                    }
                                }
                                item {
                                    Column(Modifier.onGloballyPositioned {
                                        guideTargets[1] = it.boundsInRoot()
                                    }) {
                                        NotificationControls(
                                            state = state,
                                            onIntent = onIntent,
                                            showApps = showApps,
                                            onShowAppsChange = { showApps = it },
                                            onAddPositioned = { actionTargets["wallet"] = it },
                                        )
                                    }
                                }
                            }
                            if (reviewCount > 0) item {
                                PagoCard(
                                    Modifier.fillMaxWidth().clickable(role = Role.Button) {
                                        showMovementReview = true
                                    }) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            MailReviewTitle(pulsing = hasNewMovement && !showMovementReview)
                                            Text(
                                                stringResource(
                                                    R.string.gmail_review_count,
                                                    reviewCount
                                                ), style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            stringResource(R.string.gmail_review_show),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            item {
                                Column(Modifier.onGloballyPositioned {
                                    guideTargets[2] = it.boundsInRoot()
                                }) {
                                    FlowCard(
                                        dashboard = dashboard,
                                        monthly = state.monthly,
                                        selected = state.selectedBar,
                                        onIntent = onIntent,
                                        mail = mail
                                    )
                                }
                            }
                            val movements =
                                if (mail) dashboard.movements else dashboard.receivedMovements
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().onGloballyPositioned {
                                        guideTargets[3] = it.boundsInRoot()
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        stringResource(if (mail) R.string.gmail_recent else R.string.home_recent),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    if (movements.size > RECENT_MOVEMENTS_LIMIT) {
                                        TextButton(onClick = { showAllMovements = true }) {
                                            Text(stringResource(R.string.home_view_all))
                                        }
                                    }
                                }
                            }
                            if (movements.isEmpty()) item { Text(stringResource(if (mail) R.string.gmail_empty else R.string.home_empty)) }
                            items(items = movements.take(RECENT_MOVEMENTS_LIMIT), key = { it.id }) { movement ->
                                MovementRow(
                                    movement = movement,
                                    deleting = state.savingNotification,
                                    onDelete = if (mail) ({
                                        deletingId = movement.id
                                        deletingTitle = movement.title
                                    }) else null,
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
        if (guiding) {
            HomeGuideOverlay(
                state = state,
                target = guideAction?.bounds ?: guideTargets[state.guideStep],
                onIntent = onIntent,
                permissionStep = !notificationAccessGranted && state.guideStep == 0,
                settingsFailed = settingsFailed,
                action = guideAction,
            )
        }
    }
    deletingId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text(stringResource(R.string.gmail_delete_title)) },
            text = { Text(stringResource(R.string.gmail_delete_message, deletingTitle)) },
            confirmButton = {
                TextButton(enabled = !state.savingNotification, onClick = {
                    onIntent(HomeUiIntent.DeleteGmailMovement(id))
                    deletingId = null
                }) {
                    Text(
                        stringResource(R.string.gmail_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    deletingId = null
                }) { Text(stringResource(R.string.notifications_cancel)) }
            },
        )
    }

}

private const val RECENT_MOVEMENTS_LIMIT = 3
