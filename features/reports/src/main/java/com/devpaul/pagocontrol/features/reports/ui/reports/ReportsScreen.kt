package com.devpaul.pagocontrol.features.reports.ui.reports

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.designsystem.PagoDock
import com.devpaul.pagocontrol.designsystem.PagoHeader
import com.devpaul.pagocontrol.features.reports.R

@Composable
fun ReportsScreen(state: ReportsUiState, onIntent: (ReportsUiIntent) -> Unit, onHome: () -> Unit, onMovements: () -> Unit, onProfile: () -> Unit) {
    val report = state.report
    val months = stringArrayResource(R.array.reports_months)
    val range = if (report.firstMonth == report.lastMonth) stringResource(R.string.reports_single_month, months[report.firstMonth - 1])
        else stringResource(R.string.reports_range, months[report.firstMonth - 1], months[report.lastMonth - 1])
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(state.format.mimeType)) { uri ->
        onIntent(ReportsUiIntent.Export(uri?.toString()))
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PagoDock(false, onHome, onMovements, onProfile = onProfile, reportsSelected = true) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.widthIn(max = 840.dp).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { PagoHeader(stringResource(R.string.reports_title), { onIntent(ReportsUiIntent.Notifications) }) }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportPeriod.entries.forEach { period ->
                            val label = when (period) {
                                ReportPeriod.Month -> R.string.reports_this_month
                                ReportPeriod.Quarter -> R.string.reports_last_quarter
                                ReportPeriod.Year -> R.string.reports_this_year
                                ReportPeriod.Custom -> R.string.reports_custom
                            }
                            FilterChip(
                                selected = state.period == period,
                                onClick = { onIntent(ReportsUiIntent.Period(period)) },
                                label = { Text(stringResource(label)) }, shape = CircleShape, border = null,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer, containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            )
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(range, style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.reports_demo), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item { ReportBalanceCard(report) }
                item { ReportFlowCard(report, state.selectedPoint) { onIntent(ReportsUiIntent.Point(it)) } }
                item { ReportCategoriesCard(report) }
                item { ReportChannelsCard(report) }
                item { ReportSubscriptionsCard(report) }
                item {
                    ReportCard {
                        Text(stringResource(R.string.reports_export_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.reports_export_detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReportFormat.entries.forEach { format ->
                                FilterChip(
                                    selected = state.format == format,
                                    onClick = { onIntent(ReportsUiIntent.Format(format)) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.exporting && state.pendingExport == null,
                                    label = { Text(stringResource(when (format) { ReportFormat.Pdf -> R.string.reports_format_pdf; ReportFormat.Excel -> R.string.reports_format_excel; ReportFormat.Csv -> R.string.reports_format_csv }), style = MaterialTheme.typography.labelSmall) },
                                )
                            }
                        }
                        Button(
                            onClick = {
                                onIntent(ReportsUiIntent.PrepareExport)
                                exportLauncher.launch("PagoControl-2023-${state.firstMonth}-${state.lastMonth}.${state.format.extension}")
                            },
                            enabled = !state.exporting && state.pendingExport == null,
                            modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp),
                        ) {
                            Icon(Icons.Outlined.Download, null)
                            Text(stringResource(if (state.exporting) R.string.reports_exporting else R.string.reports_download), Modifier.padding(start = 8.dp))
                        }
                        Text(stringResource(R.string.reports_export_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (state.customDialog) CustomRangeDialog(state.firstMonth, state.lastMonth, { first, last -> onIntent(ReportsUiIntent.CustomRange(first, last)) }) { onIntent(ReportsUiIntent.Dismiss) }
    state.notice?.let { notice ->
        AlertDialog(
            onDismissRequest = { onIntent(ReportsUiIntent.Dismiss) },
            title = { Text(stringResource(R.string.reports_title)) },
            text = { Text(stringResource(when (notice) { ReportsNotice.Notifications -> R.string.reports_notifications_notice; ReportsNotice.ExportSaved -> R.string.reports_export_saved; ReportsNotice.ExportError -> R.string.reports_export_error })) },
            confirmButton = { TextButton(onClick = { onIntent(ReportsUiIntent.Dismiss) }) { Text(stringResource(R.string.reports_close)) } },
        )
    }
}

@Composable
private fun CustomRangeDialog(firstMonth: Int, lastMonth: Int, onApply: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    var first by rememberSaveable { mutableIntStateOf(firstMonth) }
    var last by rememberSaveable { mutableIntStateOf(lastMonth) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(stringResource(R.string.reports_custom_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.reports_custom_detail))
                ReportMonthMenu(stringResource(R.string.reports_first_month), first) { first = it }
                ReportMonthMenu(stringResource(R.string.reports_last_month), last) { last = it }
                if (first > last) Text(stringResource(R.string.reports_invalid_range), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { Button(onClick = { onApply(first, last) }, enabled = first <= last) { Text(stringResource(R.string.reports_apply)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.reports_cancel)) } },
    )
}

@Preview(name = "Reportes claro", showBackground = true)
@Composable
private fun ReportsLightPreview() { PagoControlTheme(false) { ReportsScreen(ReportsUiState(), {}, {}, {}, {}) } }

@Preview(name = "Reportes oscuro", showBackground = true)
@Composable
private fun ReportsDarkPreview() { PagoControlTheme(true) { ReportsScreen(ReportsUiState(), {}, {}, {}, {}) } }
