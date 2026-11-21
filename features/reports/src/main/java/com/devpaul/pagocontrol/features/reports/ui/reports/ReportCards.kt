package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.reports.R
import java.util.Locale

internal fun reportPercent(value: Double) = String.format(Locale.US, "%.1f%%", value)

@Composable
internal fun ReportCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
internal fun ReportFlowCard(report: FinancialReport, onSelectMonth: () -> Unit, onOpen: (Boolean?) -> Unit) {
    var showIncome by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var showExpense by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    val direction: Boolean? = if (showIncome && showExpense) null else showIncome
    val entries = buildList {
        if (showIncome) add(PieEntry(stringResource(R.string.reports_income) + " (+)", report.income))
        if (showExpense) add(PieEntry(stringResource(R.string.reports_expense) + " (-)", report.expense))
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(selected = showIncome, onClick = { if (showExpense) showIncome = !showIncome }, label = { Text(stringResource(R.string.reports_income) + " (+)") })
            FilterChip(selected = showExpense, onClick = { if (showIncome) showExpense = !showExpense }, label = { Text(stringResource(R.string.reports_expense) + " (-)") })
        }
        ReportPieCard(stringResource(R.string.reports_tab_flow), entries, income = direction, onSelectMonth = onSelectMonth, onOpenAll = { onOpen(direction) }) { onOpen(direction) }
    }
}

@Composable
internal fun ReportCategoriesCard(report: FinancialReport, onSelectMonth: () -> Unit, onOpen: (String) -> Unit) {
    val categories = report.categories.mapNotNull { it.category }
    val entries = report.categories.filter { it.cents > 0 }
    ReportPieCard(stringResource(R.string.reports_categories), entries.map { PieEntry(it.category?.displayName() ?: stringResource(it.label), it.cents, it.category?.chartColor(categories)) }, income = false, onSelectMonth = onSelectMonth) { index -> onOpen(entries[index].category?.name.orEmpty()) }
}

@Composable
internal fun ReportChannelsCard(report: FinancialReport, onSelectMonth: () -> Unit, onOpen: (Boolean, String) -> Unit) {
    var income by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(selected = income, onClick = { income = true }, label = { Text(stringResource(R.string.reports_income) + " (+)") })
            FilterChip(selected = !income, onClick = { income = false }, label = { Text(stringResource(R.string.reports_expense) + " (-)") })
        }
        Text(stringResource(R.string.reports_wallet_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val entries = report.channels.map { PieEntry(it.source, if (income) it.income else it.expense) }.filter { it.cents > 0 }
        ReportPieCard(stringResource(R.string.reports_channels), entries, income, onSelectMonth = onSelectMonth) { index -> onOpen(income, entries[index].name) }
    }
}

@Composable
internal fun ReportMonthMenu(label: String, selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val months = stringArrayResource(R.array.reports_months)
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${months[selected - 1]}", Modifier.weight(1f))
            Icon(Icons.Outlined.ExpandMore, null)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            months.forEachIndexed { index, month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = { expanded = false; onSelect(index + 1) })
            }
        }
    }
}
