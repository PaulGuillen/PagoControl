package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.reports.R

@Composable
internal fun ReportNavigationCard(title: String, value: String, detail: String, centered: Boolean = false, onMapped: (() -> Unit)? = null, onSelectMonth: (() -> Unit)? = null, showChevron: Boolean = true, onClick: () -> Unit) {
    if (centered) {
        ReportCard {
            Column(Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(value, style = MaterialTheme.typography.headlineMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(detail, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (onMapped != null) IconButton(onClick = onMapped) { Icon(Icons.Outlined.Subscriptions, stringResource(R.string.reports_mapped_subscriptions)) }
                }
                    if (onMapped != null) IconButton(onClick = onMapped) {
                        Icon(Icons.Outlined.Subscriptions, stringResource(R.string.reports_mapped_subscriptions))
                    }
                }
                TextButton(onClick = onClick) { Text(stringResource(R.string.reports_view_detail)) }
            }
        }
        return
    }
    ReportCard {
        Column(Modifier.fillMaxWidth().clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportSectionTitle(title, onSelectMonth)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(value, style = MaterialTheme.typography.headlineMedium)
                    Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (showChevron) Icon(Icons.Outlined.ChevronRight, stringResource(R.string.reports_view_detail))
            }
        }
        TextButton(onClick = onClick) { Text(stringResource(R.string.reports_view_detail)) }
    }
}

@Composable
internal fun ReportDetailScreen(page: String, state: ReportsUiState, onIntent: (ReportsUiIntent) -> Unit, navigate: (String) -> Unit, back: () -> Unit, pointFilter: Int? = null, channelFilter: String = "", categoryFilter: String = "") {
    var query by rememberSaveable(page) { mutableStateOf("") }
    var selectedMapped by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedExpenseKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSubscription by rememberSaveable { mutableStateOf<String?>(null) }
    val scroll = rememberLazyListState()
    val report = state.report
    val title = stringResource(when (page) {
        "Flow" -> R.string.reports_tab_flow
        "Income" -> R.string.reports_income
        "Expenses" -> R.string.reports_expense
        "Catalog" -> R.string.reports_mapped_subscriptions
        "Subscriptions" -> R.string.reports_subscriptions
        else -> R.string.reports_recorded_expenses
    })
    val normalizedQuery = normalizeReportText(query)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.reports_back)) }
                    Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Box(Modifier.size(48.dp))
                }
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp), singleLine = true,
                    label = { Text(stringResource(R.string.reports_search)) })
            }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), state = scroll, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (page == "Flow") {
                val groups = report.flowMovements(query)
                val formatter = java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", java.util.Locale.forLanguageTag("es-PE"))
                item(key = "flow-summary") {
                    ReportCard {
                        Text("${report.start} - ${report.end}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.reports_income), style = MaterialTheme.typography.labelMedium)
                                Text(formatSoles(groups.values.flatten().filter { it.cents > 0 }.sumOf { it.cents }, signed = true), style = MaterialTheme.typography.titleMedium)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.reports_expense), style = MaterialTheme.typography.labelMedium)
                                Text(formatSoles(groups.values.flatten().filter { it.cents < 0 }.sumOf { it.cents }, signed = true), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                if (groups.isEmpty()) item { Text(stringResource(R.string.reports_chart_empty)) }
                groups.forEach { (date, movements) ->
                    item(key = "day-$date") {
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(date.format(formatter), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatSoles(movements.sumOf { it.cents }, signed = true), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(movements, key = { it.key }) { movement ->
                        DirectionMovementCard(movement.title, movement.cents, movement.date.toString(), movement.channel)
                    }
                }
            } else if (page == "Income" || page == "Expenses") {
                val incoming = page == "Income"
                val incomes = report.incomes.filter { report.matchesPoint(it.date, pointFilter) && (channelFilter.isBlank() || it.channel == channelFilter) && normalizeReportText("${it.title} ${it.channel}").contains(normalizedQuery) }
                val expenses = report.expenses.filter { report.matchesPoint(it.date, pointFilter) && (channelFilter.isBlank() || it.channel == channelFilter) && (categoryFilter.isBlank() || it.category.name == categoryFilter) && normalizeReportText("${it.title} ${it.channel}").contains(normalizedQuery) }
                item(key = "direction-total") {
                    ReportCard {
                        Text(title + if (incoming) " (+)" else " (-)", style = MaterialTheme.typography.titleLarge)
                        Text(formatSoles(if (incoming) incomes.sumOf { it.cents } else -expenses.sumOf { it.cents }, signed = true), style = MaterialTheme.typography.headlineLarge, color = if (incoming) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
                        Text("${report.start} - ${report.end}")
                        if (pointFilter != null) report.points.getOrNull(pointFilter)?.let { point -> Text(if (report.weekly) "Semana ${pointFilter + 1}" else "Mes ${point.number}") }
                        if (channelFilter.isNotBlank()) Text(channelFilter)
                        if (categoryFilter.isNotBlank()) Text(com.devpaul.pagocontrol.domain.ExpenseCategory.valueOf(categoryFilter).displayName())
                    }
                }
                if (incoming) {
                    if (incomes.isEmpty()) item { Text(stringResource(R.string.reports_chart_empty)) }
                    items(incomes, key = { it.id }) { movement ->
                        DirectionMovementCard(movement.title, movement.cents, movement.date.toString(), movement.channel)
                    }
                } else {
                    if (expenses.isEmpty()) item { Text(stringResource(R.string.reports_chart_empty)) }
                    items(expenses, key = { "${it.manual}-${it.id}" }) { movement ->
                        DirectionMovementCard(movement.title, -movement.cents, movement.date.toString(), movement.channel)
                    }
                }
            } else if (page == "Subscriptions") {
                item(key = "total") {
                    ReportCard {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.reports_subscription_total), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { navigate("Catalog") }) { Icon(Icons.Outlined.Subscriptions, stringResource(R.string.reports_mapped_subscriptions)) }
                        }
                        Text(formatSoles(-report.subscriptions.sumOf { it.cents }, signed = true), style = MaterialTheme.typography.headlineLarge)
                        Text("${report.start} - ${report.end}", style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.reports_subscription_detail), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                val subscriptions = subscriptionList(report, state.settings).filter { normalizeReportText(it.name).contains(normalizedQuery) }
                if (subscriptions.isEmpty()) item { Text(stringResource(R.string.reports_no_subscriptions)) }
                items(subscriptions, key = { "subscription-${it.name}" }) { subscription ->
                    ReportCard(Modifier.clickable(role = Role.Button, onClickLabel = stringResource(R.string.reports_view_detail)) { selectedSubscription = subscription.name }) {
                        Text(subscription.name, style = MaterialTheme.typography.titleLarge)
                        Text(formatSoles(-subscription.cents, signed = true), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.reports_operations, subscription.payments), style = MaterialTheme.typography.bodyMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { selectedSubscription = subscription.name }) {
                                Icon(Icons.Outlined.Visibility, null)
                                Text(stringResource(R.string.reports_view_detail), Modifier.padding(start = 8.dp))
                            }
                            TextButton(enabled = !state.saving, onClick = { onIntent(ReportsUiIntent.SubscriptionVisibility(subscription.name, true)) }) {
                                Icon(Icons.Outlined.Delete, null)
                                Text(stringResource(R.string.reports_remove_subscription), Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            } else if (page == "Catalog") {
                item { Text(stringResource(R.string.reports_catalog_help), style = MaterialTheme.typography.bodyLarge) }
                items(SubscriptionCatalog.services.filter { normalizeReportText("${it.name} ${it.group}").contains(normalizedQuery) }, key = { it.name }) { service ->
                    ReportCard(Modifier.clickable(role = Role.Button, onClickLabel = stringResource(R.string.reports_view_detail)) { selectedMapped = service.name }) {
                        Text(service.name, style = MaterialTheme.typography.titleLarge)
                        Text(service.group, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { selectedMapped = service.name }) {
                            Icon(Icons.Outlined.Visibility, null)
                            Text(stringResource(R.string.reports_view_detail), Modifier.padding(start = 8.dp))
                        }
                    }
                }
            } else {
                item { Text(stringResource(R.string.reports_expense_list_detail), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                item { ReportCategoryManager(state, onIntent) }
                val expenses = report.expenses.filter { normalizeReportText("${it.title} ${it.channel}").contains(normalizedQuery) }
                if (expenses.isEmpty()) item { Text(stringResource(R.string.reports_chart_empty)) }
                items(expenses, key = { "${it.manual}-${it.id}" }) { expense ->
                    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.fillMaxWidth()
                                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.reports_email_description)) { selectedExpenseKey = "${expense.manual}-${expense.id}" }
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Text(expense.title, style = MaterialTheme.typography.titleLarge)
                                Text(formatSoles(-expense.cents, signed = true), style = MaterialTheme.typography.headlineSmall)
                                Text("${expense.date} - ${expense.channel}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                CategoryMenu(expense.category, !state.saving, com.devpaul.pagocontrol.domain.ExpenseCategory.entries + state.settings.customCategories, modifier = Modifier.weight(1f)) { category ->
                                    if (category != expense.category) {
                                        if (expense.manual) state.settings.expenses.find { it.id == expense.id }?.let { onIntent(ReportsUiIntent.SaveExpense(it.copy(category = category))) }
                                        else onIntent(ReportsUiIntent.Category(expense.id, category))
                                    }
                                }
                                if (expense.manual) IconButton(enabled = !state.saving, onClick = { onIntent(ReportsUiIntent.DeleteExpense(expense.id)) }) {
                                    Icon(Icons.Outlined.Delete, stringResource(R.string.reports_delete_expense))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    selectedExpenseKey?.let { selected ->
        report.expenses.find { "${it.manual}-${it.id}" == selected }?.let { expense ->
            val original = if (expense.manual) null else state.movements.find { it.id == expense.id }
            AlertDialog(
                onDismissRequest = { selectedExpenseKey = null },
                title = { Text(stringResource(R.string.reports_email_description)) },
                text = {
                    SelectionContainer {
                        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(expense.title, style = MaterialTheme.typography.titleLarge)
                            Text(formatSoles(-expense.cents, signed = true), style = MaterialTheme.typography.headlineSmall)
                            Text("${expense.date} - ${expense.channel}")
                            Text(expense.category.displayName(), style = MaterialTheme.typography.labelLarge)
                            HorizontalDivider()
                            original?.notificationTitle?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                            Text(original?.notificationText?.takeIf { it.isNotBlank() } ?: if (expense.manual) expense.title else stringResource(R.string.reports_no_email_description), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { selectedExpenseKey = null }) { Text(stringResource(R.string.reports_close)) } },
            )
        }
    }
    selectedMapped?.let { name ->
        SubscriptionCatalog.services.find { it.name == name }?.let { service ->
            AlertDialog(
                onDismissRequest = { selectedMapped = null },
                title = { Text(service.name) },
                text = {
                    Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(service.group, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.reports_mapped_detail))
                        service.aliases.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
                    }
                },
                confirmButton = { TextButton(onClick = { selectedMapped = null }) { Text(stringResource(R.string.reports_close)) } },
            )
        }
    }
    selectedSubscription?.let { name ->
        SubscriptionDetailModal(name, report, state.saving, { selectedSubscription = null }) {
            onIntent(ReportsUiIntent.SubscriptionVisibility(name, true))
            selectedSubscription = null
        }
    }
}

@Composable
private fun SubscriptionDetailModal(name: String, report: FinancialReport, saving: Boolean, onDismiss: () -> Unit, onRemove: () -> Unit) {
    val payments = report.expenses.filter { it.subscription == name }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.reports_subscription_total), style = MaterialTheme.typography.titleMedium)
                Text(formatSoles(-payments.sumOf { it.cents }, signed = true), style = MaterialTheme.typography.headlineLarge)
                Text("${report.start} - ${report.end}")
                if (payments.isEmpty()) Text(stringResource(R.string.reports_chart_empty))
                payments.forEach { payment ->
                    HorizontalDivider()
                    Text(formatSoles(-payment.cents, signed = true), style = MaterialTheme.typography.titleMedium)
                    Text("${payment.date} - ${payment.channel}", style = MaterialTheme.typography.bodyMedium)
                    Text(payment.title, style = MaterialTheme.typography.bodyMedium)
                }
                Text(stringResource(R.string.reports_remove_subscription_note), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.reports_close)) } },
        dismissButton = { TextButton(enabled = !saving, onClick = onRemove) { Text(stringResource(R.string.reports_remove_subscription)) } },
    )
}

@Composable
private fun DirectionMovementCard(title: String, signedCents: Long, date: String, channel: String) {
    ReportCard {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(formatSoles(signedCents, signed = true), style = MaterialTheme.typography.headlineSmall, color = if (signedCents > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
        Text("$date - $channel", style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(if (signedCents > 0) R.string.reports_income else R.string.reports_expense), style = MaterialTheme.typography.labelLarge)
    }
}
