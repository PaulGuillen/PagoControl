package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.designsystem.PagoDock
import com.devpaul.pagocontrol.designsystem.PagoHeader
import com.devpaul.pagocontrol.features.reports.R
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onIntent: (ReportsUiIntent) -> Unit,
    onHome: () -> Unit,
    onMovements: () -> Unit,
    onProfile: () -> Unit
) {
    var page by rememberSaveable { mutableStateOf("Overview") }
    val pages = rememberSaveableStateHolder()
    var selectionParent by rememberSaveable { mutableStateOf("Subscriptions") }
    fun navigateReport(next: String) {
        if (next == "Catalog") selectionParent = page
        page = next
    }
    fun backReport() { page = if (page == "Catalog") selectionParent else "Overview" }
    var management by rememberSaveable { mutableIntStateOf(0) }
    var detailPoint by rememberSaveable { mutableIntStateOf(-1) }
    var detailChannel by rememberSaveable { mutableStateOf("") }
    var detailCategory by rememberSaveable { mutableStateOf("") }
    fun openMovements(income: Boolean, point: Int? = null, channel: String = "", category: String = "") {
        detailPoint = point ?: -1
        detailChannel = channel
        detailCategory = category
        page = if (income) "Income" else "Expenses"
    }
    var chart by rememberSaveable { mutableIntStateOf(0) }
    val overviewScroll = rememberLazyListState()
    BackHandler(page != "Overview") { backReport() }
    val report = state.report
    val selectMonth = { onIntent(ReportsUiIntent.Period(ReportPeriod.Custom)) }
    val range = "${report.start.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE")))} – ${report.end.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE")))}"
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(state.format.mimeType)) { uri ->
            onIntent(ReportsUiIntent.Export(uri?.toString()))
        }
    if (page != "Overview") pages.SaveableStateProvider(if (page == "Income" || page == "Expenses") "$page:$detailPoint:$detailChannel:$detailCategory" else page) {
        ReportDetailScreen(page, state, onIntent, { navigateReport(it) }, { backReport() }, detailPoint.takeIf { it >= 0 }, detailChannel, detailCategory)
    }
    else Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PagoDock(
                false,
                onHome,
                onMovements,
                onProfile = onProfile,
                reportsSelected = true
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.widthIn(max = 840.dp).fillMaxSize(),
                state = overviewScroll,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    PagoHeader(stringResource(R.string.reports_title))
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(range, style = MaterialTheme.typography.labelMedium)
                        Text(
                            stringResource(R.string.reports_real_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (state.loading) item { CircularProgressIndicator() }
                if (state.error) item {
                    ReportCard {
                        Text(stringResource(R.string.reports_load_error))
                        TextButton(onClick = { onIntent(ReportsUiIntent.Retry) }) { Text(stringResource(R.string.reports_retry)) }
                    }
                }
                if (!state.loading && !state.error && report.expenses.isEmpty() && report.incomeCount == 0) item {
                    Text(stringResource(R.string.reports_empty), style = MaterialTheme.typography.bodyMedium)
                }
                item(key = "chart-tabs") { ReportChartTabs(chart) { chart = it } }
                item(key = "selected-chart") {
                    when (chart) {
                        0 -> ReportFlowCard(report, selectMonth) { income -> if (income == null) page = "Flow" else openMovements(income) }
                        1 -> ReportCategoriesCard(report, selectMonth) { openMovements(false, category = it) }
                        else -> ReportChannelsCard(report, selectMonth) { income, channel -> openMovements(income, channel = channel) }
                    }
                }
                item(key = "management-tabs") {
                    ReportOptionTabs(listOf(R.string.reports_recorded_expenses, R.string.reports_subscriptions), listOf(Icons.Outlined.Category, Icons.Outlined.Subscriptions), management) { management = it }
                }
                item(key = "management-content") {
                    if (management == 1) ReportNavigationCard(stringResource(R.string.reports_subscriptions), formatSoles(-report.subscriptions.sumOf { it.cents }, signed = true), stringResource(R.string.reports_subscription_total), onMapped = { navigateReport("Catalog") }, onSelectMonth = selectMonth, showChevron = false) { page = "Subscriptions" }
                    else ReportNavigationCard(stringResource(R.string.reports_recorded_expenses), formatSoles(-report.expense, signed = true), stringResource(R.string.reports_expense_list_detail), onSelectMonth = selectMonth, showChevron = false) { page = "Classify" }
                }
                item {
                    ReportCard {
                        Text(
                            stringResource(R.string.reports_export_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.reports_export_detail),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportFormat.entries.forEach { format ->
                                FilterChip(
                                    selected = state.format == format,
                                    onClick = { onIntent(ReportsUiIntent.Format(format)) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.loading && !state.error && !state.exporting && state.pendingExport == null,
                                    label = {
                                        Text(
                                            stringResource(
                                                when (format) {
                                                    ReportFormat.Pdf -> R.string.reports_format_pdf; ReportFormat.Excel -> R.string.reports_format_excel; ReportFormat.Csv -> R.string.reports_format_csv
                                                }
                                            ), style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                )
                            }
                        }
                        Button(
                            onClick = {
                                onIntent(ReportsUiIntent.PrepareExport)
                                exportLauncher.launch("PagoControl-${report.start}-${report.end}.${state.format.extension}")
                            },
                            enabled = !state.loading && !state.error && !state.exporting && state.pendingExport == null,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                        ) {
                            Icon(Icons.Outlined.Download, null)
                            Text(
                                stringResource(if (state.exporting) R.string.reports_exporting else R.string.reports_download),
                                Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.reports_export_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    if (state.dialog == ReportDialog.Expense) ReportExpenseDialog(state, onIntent)
    if (state.dialog == ReportDialog.Subscription) ReportSubscriptionDialog(state, onIntent)
    if (state.customDialog) ReportMonthDialog(
        selectedMonth = if (state.period == ReportPeriod.Month) state.referenceDate.monthValue else state.firstMonth,
        year = state.referenceDate.year,
        onApply = { month -> onIntent(ReportsUiIntent.CustomRange(month, month)) },
        onDismiss = { onIntent(ReportsUiIntent.Dismiss) },
    )
    state.notice?.let { notice ->
        AlertDialog(
            onDismissRequest = { onIntent(ReportsUiIntent.DismissNotice) },
            title = { Text(stringResource(R.string.reports_title)) },
            text = {
                Text(
                    stringResource(
                        when (notice) {
                            ReportsNotice.ExportSaved -> R.string.reports_export_saved; ReportsNotice.ExportError -> R.string.reports_export_error; ReportsNotice.SaveError -> R.string.reports_save_error
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onIntent(ReportsUiIntent.DismissNotice) }) {
                    Text(
                        stringResource(R.string.reports_close)
                    )
                }
            },
        )
    }
}

@Preview(name = "Reportes claro", showBackground = true)
@Composable
private fun ReportsLightPreview() {
    PagoControlTheme(false) { ReportsScreen(ReportsUiState(), {}, {}, {}, {}) }
}

@Preview(name = "Reportes oscuro", showBackground = true)
@Composable
private fun ReportsDarkPreview() {
    PagoControlTheme(true) { ReportsScreen(ReportsUiState(), {}, {}, {}, {}) }
}
