package com.devpaul.pagocontrol.features.home.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun HomeScreen(state: HomeUiState, name: String, onIntent: (HomeUiIntent) -> Unit, themeIndex: Int, onThemeSelected: (Int) -> Unit, onMovements: () -> Unit = {}, onProfile: () -> Unit = {}, onReports: () -> Unit = {}) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { com.devpaul.pagocontrol.designsystem.PagoDock(false, {}, onMovements, onProfile = onProfile, onReports = onReports) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
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
                    }
                }
                item { HomeSectionTabs(section = state.section, onIntent = onIntent) }
                if (state.section == HomeSection.Mail) {
                    item { MailTransactionsCard() }
                } else when (val content = state.content) {
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
                        item { ReceivedPaymentsCard(dashboard) }
                        item { NotificationControls(state = state, onIntent = onIntent) }
                        val pending = state.pendingNotifications
                        items(items = pending, key = { "notification-${it.id}" }) { notification ->
                            PendingNotificationCard(notification = notification, saving = state.savingNotification, onIntent = onIntent)
                        }
                        item { FlowCard(dashboard = dashboard, monthly = state.monthly, selected = state.selectedBar, onIntent = onIntent) }
                        item {
                            Text(stringResource(R.string.home_recent), style = MaterialTheme.typography.headlineSmall)
                        }
                        val movements = dashboard.receivedMovements
                        if (movements.isEmpty()) item { Text(stringResource(R.string.home_empty)) }
                        items(items = movements, key = { it.id }) { MovementRow(it) }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
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
private fun MovementRow(movement: Movement) {
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
