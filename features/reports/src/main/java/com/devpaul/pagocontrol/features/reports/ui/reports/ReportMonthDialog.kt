package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.reports.R

@Composable
internal fun ReportSectionTitle(title: String, onSelectMonth: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        if (onSelectMonth != null) IconButton(onClick = onSelectMonth) {
            Icon(Icons.Outlined.CalendarMonth, stringResource(R.string.reports_select_month), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ReportMonthDialog(selectedMonth: Int, year: Int, onApply: (Int) -> Unit, onDismiss: () -> Unit) {
    var month by rememberSaveable { mutableIntStateOf(selectedMonth) }
    val months = stringArrayResource(R.array.reports_months)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reports_select_month)) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(year.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                (1..12).chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { value ->
                            FilterChip(
                                selected = month == value,
                                onClick = { month = value },
                                modifier = Modifier.weight(1f),
                                label = { Text(months[value - 1], style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onApply(month) }) { Text(stringResource(R.string.reports_apply)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.reports_cancel)) } },
    )
}
