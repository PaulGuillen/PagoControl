package com.devpaul.pagocontrol.features.home.ui.home

import android.content.Intent
import android.provider.Settings
import com.devpaul.pagocontrol.features.home.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.designsystem.InstitutionAvatar
import com.devpaul.pagocontrol.designsystem.InstitutionBadge
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.domain.Movement


@Composable
fun HomeScreen(state: HomeUiState, name: String, onIntent: (HomeUiIntent) -> Unit, themeIndex: Int, onThemeSelected: (Int) -> Unit, onMovements: () -> Unit = {}, onProfile: () -> Unit = {}, onReports: () -> Unit = {}, notificationAccessGranted: Boolean = rememberNotificationAccess()) {
    val context = LocalContext.current
    var settingsFailed by remember { mutableStateOf(false) }
    val activateAccess: () -> Unit = {
        settingsFailed = runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }.isFailure
    }
    var previousAccess by remember { mutableStateOf(notificationAccessGranted) }
    LaunchedEffect(notificationAccessGranted) {
        if (notificationAccessGranted && !previousAccess && state.guideStep == 0) onIntent(HomeUiIntent.NextGuideStep)
        previousAccess = notificationAccessGranted
    }
    val mail = state.section == HomeSection.Mail
    val listState = rememberLazyListState()
    val ready = (if (mail) state.gmailContent else state.content) is HomeContent.Ready
    val pendingCount = if (mail) 0 else state.pendingNotifications.size
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingTitle by rememberSaveable { mutableStateOf("") }
    val guideTargets = remember(state.section, notificationAccessGranted) { mutableStateMapOf<Int, Rect>() }
    val actionTargets = remember(state.section, notificationAccessGranted) { mutableStateMapOf<String, Rect>() }
    val accessButtonRequester = remember { BringIntoViewRequester() }
    var showApps by rememberSaveable { mutableStateOf(false) }
    var gmailSwitchEnabled by remember(state.section) { mutableStateOf(false) }
    val guiding = ready && state.guideStep != null && !showApps
    val guideAction = when {
        state.guideStep == 0 && !notificationAccessGranted -> actionTargets["access"]?.let {
            HomeGuideAction(bounds = it, label = stringResource(R.string.notifications_access), onClick = activateAccess)
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
            HomeGuideAction(bounds = it, label = stringResource(R.string.notifications_add_apps), onClick = { showApps = true })
        }
        else -> null
    }
    LaunchedEffect(state.section, state.guideStep, ready, notificationAccessGranted) {
        if (ready && state.guideStep != null) {
            val index = when (state.guideStep) {
                0 -> if (notificationAccessGranted) 3 else 2
                1 -> (if (mail) 5 else 4) + if (notificationAccessGranted) 0 else 1
                2 -> (if (mail) 6 else 5) + pendingCount + if (notificationAccessGranted) 0 else 1
                else -> (if (mail) 7 else 6) + pendingCount + if (notificationAccessGranted) 0 else 1
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
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (guiding) Modifier.clearAndSetSemantics {} else Modifier,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { com.devpaul.pagocontrol.designsystem.PagoDock(false, {}, onMovements, onProfile = onProfile, onReports = onReports) },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        com.devpaul.pagocontrol.designsystem.PagoHeader(stringResource(R.string.home_title), { onIntent(HomeUiIntent.Upcoming) })
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.home_greeting, name), style = MaterialTheme.typography.headlineMedium)
                            Text(text = stringResource(R.string.home_demo), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { onIntent(HomeUiIntent.ShowGuide) }) {
                                Text(stringResource(R.string.home_guide_help))
                            }
                        }
                    }
                    if (!notificationAccessGranted) {
                        item(key = "notification-access") {
                            Column(Modifier.onGloballyPositioned { guideTargets[0] = it.boundsInRoot() }) {
                                NotificationAccessCard(
                                    settingsFailed = settingsFailed,
                                    onActivate = activateAccess,
                                    actionModifier = Modifier.bringIntoViewRequester(accessButtonRequester)
                                        .onGloballyPositioned { actionTargets["access"] = it.boundsInRoot() },
                                )
                            }
                        }
                    }
                    item { HomeSectionTabs(section = state.section, onIntent = onIntent) }
                    when (val content = if (mail) state.gmailContent else state.content) {
                        HomeContent.Loading -> item {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        HomeContent.Error -> item {
                            PagoCard(Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.home_error))
                                Button(onClick = { onIntent(HomeUiIntent.Retry) }) { Text(stringResource(R.string.home_retry)) }
                            }
                        }
                        is HomeContent.Ready -> {
                            val dashboard = content.dashboard
                            if (mail) {
                                item {
                                    Column(Modifier.onGloballyPositioned { if (notificationAccessGranted) guideTargets[0] = it.boundsInRoot() }) {
                                        MailBalanceCard(dashboard)
                                    }
                                }
                                item {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        MailSummaryCard(income = true, dashboard = dashboard, modifier = Modifier.weight(1f))
                                        MailSummaryCard(income = false, dashboard = dashboard, modifier = Modifier.weight(1f))
                                    }
                                }
                                item {
                                    Column(Modifier.onGloballyPositioned { guideTargets[1] = it.boundsInRoot() }) {
                                        GmailMonitoringCard(
                                            state = state,
                                            onIntent = onIntent,
                                            access = notificationAccessGranted,
                                            switchModifier = Modifier.onGloballyPositioned { actionTargets["gmail"] = it.boundsInRoot() },
                                            onSwitchEnabled = { gmailSwitchEnabled = it },
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Column(Modifier.onGloballyPositioned { if (notificationAccessGranted) guideTargets[0] = it.boundsInRoot() }) {
                                        ReceivedPaymentsCard(dashboard)
                                    }
                                }
                                item {
                                    Column(Modifier.onGloballyPositioned { guideTargets[1] = it.boundsInRoot() }) {
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
                            val pending = if (mail) emptyList() else state.pendingNotifications
                            items(items = pending, key = { "notification-${it.id}" }) { notification ->
                                PendingNotificationCard(notification = notification, saving = state.savingNotification, onIntent = onIntent, mail = mail)
                            }
                            item {
                                Column(Modifier.onGloballyPositioned { guideTargets[2] = it.boundsInRoot() }) {
                                    FlowCard(dashboard = dashboard, monthly = state.monthly, selected = state.selectedBar, onIntent = onIntent)
                                }
                            }
                            item {
                                Column(Modifier.onGloballyPositioned { guideTargets[3] = it.boundsInRoot() }) {
                                    Text(stringResource(if (mail) R.string.gmail_recent else R.string.home_recent), style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            val movements = if (mail) dashboard.movements else dashboard.receivedMovements
                            if (movements.isEmpty()) item { Text(stringResource(if (mail) R.string.gmail_empty else R.string.home_empty)) }
                            items(items = movements, key = { it.id }) { movement ->
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
                }) { Text(stringResource(R.string.gmail_delete_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) { Text(stringResource(R.string.notifications_cancel)) }
            },
        )
    }
    if (state.showUpcoming) {
        AlertDialog(
            onDismissRequest = { onIntent(HomeUiIntent.DismissUpcoming) },
            title = { Text(stringResource(R.string.home_upcoming_title)) },
            text = { Text(stringResource(R.string.home_upcoming_message)) },
            confirmButton = {
                TextButton(onClick = { onIntent(HomeUiIntent.DismissUpcoming) }) {
                    Text(stringResource(R.string.home_understood), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}

@Composable
private fun MailBalanceCard(dashboard: Dashboard) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.gmail_balance), style = MaterialTheme.typography.labelLarge)
            Text(formatSoles(dashboard.balanceCents), style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.gmail_balance_hint), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MailSummaryCard(income: Boolean, dashboard: Dashboard, modifier: Modifier) {
    val tint = if (income) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val amount = if (income) dashboard.incomeCents else -dashboard.expenseCents
    val count = if (income) dashboard.incomeCount else dashboard.expenseCount
    PagoCard(modifier) {
        Icon(imageVector = if (income) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward, contentDescription = null, tint = tint)
        Text(stringResource(if (income) R.string.gmail_income else R.string.gmail_expense), style = MaterialTheme.typography.bodySmall)
        Text(text = formatSoles(amount, signed = true), color = tint, style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(if (income) R.string.home_income_count else R.string.gmail_expense_count, count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReceivedPaymentsCard(dashboard: Dashboard) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.home_balance), style = MaterialTheme.typography.labelLarge)
            Text(formatSoles(dashboard.incomeCents), style = MaterialTheme.typography.displaySmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.home_income_count, dashboard.incomeCount), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FlowCard(dashboard: Dashboard, monthly: Boolean, selected: Int, onIntent: (HomeUiIntent) -> Unit) {
    val values = if (monthly) dashboard.monthlyIncomeFlow else dashboard.weeklyIncomeFlow
    val labels = stringArrayResource(if (monthly) R.array.home_weeks else R.array.home_days)
    PagoCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.home_flow), style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(false, true).forEach { isMonthly ->
                Surface(
                    color = if (monthly == isMonthly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (monthly == isMonthly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.semantics { this.selected = monthly == isMonthly }
                        .clickable(role = Role.Tab) { onIntent(HomeUiIntent.SelectPeriod(isMonthly)) },
                ) {
                    Text(text = stringResource(if (isMonthly) R.string.home_month else R.string.home_week), modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Text(
            text = "${labels.getOrElse(selected) { "" }} · ${formatSoles(values.getOrElse(selected) { 0 }, signed = true)}",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            val maximum = (values.maxOfOrNull { kotlin.math.abs(it) } ?: 1L).coerceAtLeast(1L)
            values.forEachIndexed { index, amount ->
                val label = labels.getOrElse(index) { "" }
                val description = "$label: ${formatSoles(amount)}"
                Column(
                    modifier = Modifier.weight(1f).clip(MaterialTheme.shapes.small)
                        .clickable(role = Role.Button) { onIntent(HomeUiIntent.SelectBar(index)) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = description
                            this.selected = selected == index
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(
                            Modifier.fillMaxWidth(0.8f).height((24 + 96 * kotlin.math.abs(amount).toFloat() / maximum).dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
                        )
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun MovementRow(movement: Movement, deleting: Boolean = false, onDelete: (() -> Unit)? = null) {
    PagoCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InstitutionAvatar(movement.source)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movement.title, style = MaterialTheme.typography.titleMedium)
                Text(text = movement.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                InstitutionBadge(movement.source)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(max = 130.dp)) {
                Text(
                    text = formatSoles(movement.amountCents, signed = true), style = MaterialTheme.typography.titleMedium,
                    color = if (movement.amountCents > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                )
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, enabled = !deleting) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.gmail_delete_action, movement.title))
                }
            }
        }
    }
}


@Preview(name = "Home claro", showBackground = true)
@Composable
private fun HomeLightPreview() { PagoControlTheme(false) { HomeScreen(state = previewState(), name = "Carlos", onIntent = {}, themeIndex = 1, onThemeSelected = {}) } }

@Preview(name = "Home oscuro", showBackground = true)
@Composable
private fun HomeDarkPreview() { PagoControlTheme(true) { HomeScreen(state = previewState(), name = "Carlos", onIntent = {}, themeIndex = 2, onThemeSelected = {}) } }

private fun previewState() = HomeUiState(content = HomeContent.Ready(
    Dashboard(balanceCents = 458050, incomeCents = 685000, expenseCents = 226950, incomeCount = 18, expenseCount = 34, weeklyFlow = listOf(21, 28, 17, 35, 24, 42, 14), monthlyFlow = listOf(14, 18, 16, 20), movements = emptyList()),
))
