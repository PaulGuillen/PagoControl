package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.InstitutionAvatar
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.reports.R
import java.util.Locale

internal fun reportPercent(value: Double) = String.format(Locale.US, "%.1f%%", value)

@Composable
internal fun ReportCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
internal fun ReportBalanceCard(report: FinancialReport) {
    val colors = MaterialTheme.colorScheme
    ReportCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.reports_net_balance), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            report.change?.let { change ->
                Surface(shape = CircleShape, color = colors.secondaryContainer) {
                    Text(stringResource(R.string.reports_change, (if (change > 0) "+" else "") + reportPercent(change)), Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                }
            }
        }
        Text(formatSoles(report.net, true), style = MaterialTheme.typography.headlineLarge, color = if (report.net >= 0) colors.secondary else colors.tertiary)
        Surface(color = colors.surfaceContainerLow, shape = RoundedCornerShape(14.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Savings, null, Modifier.size(24.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.reports_savings), style = MaterialTheme.typography.labelMedium)
                    Text(stringResource(R.string.reports_savings_detail), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                Text(reportPercent(report.savingsRate), style = MaterialTheme.typography.titleMedium, color = colors.secondary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportMetric(true, report.income, report.incomeCount, Modifier.weight(1f))
            ReportMetric(false, report.expense, report.expenseCount, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ReportMetric(income: Boolean, cents: Long, count: Int, modifier: Modifier) {
    val tint = if (income) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(if (income) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward, null, Modifier.size(16.dp), tint = tint)
                Text(stringResource(if (income) R.string.reports_income else R.string.reports_expense), style = MaterialTheme.typography.labelMedium)
            }
            Text(formatSoles(if (income) cents else -cents, true), style = MaterialTheme.typography.titleMedium, color = tint)
            Text(stringResource(R.string.reports_transactions, count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ReportFlowCard(report: FinancialReport, selected: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val months = stringArrayResource(R.array.reports_short_months)
    val point = report.points.getOrElse(selected) { report.points.first() }
    fun pointLabel(number: Int): String = if (report.weekly) "Sem $number" else months[number - 1]
    ReportCard {
        Text(stringResource(if (report.weekly) R.string.reports_weekly_flow else R.string.reports_monthly_flow), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Legend(stringResource(R.string.reports_income), colors.secondary)
            Legend(stringResource(R.string.reports_expense), colors.tertiary)
        }
        Surface(shape = RoundedCornerShape(16.dp), color = colors.surfaceContainerLow) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(pointLabel(point.number), style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.reports_point_amounts, formatSoles(point.income), formatSoles(point.expense)), style = MaterialTheme.typography.labelSmall)
                    }
                }
                BoxWithConstraints {
                    val chartWidth = maxOf(maxWidth, (report.points.size * 54).dp)
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        Row(Modifier.width(chartWidth), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                            val maximum = report.points.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1)
                            report.points.forEachIndexed { index, value ->
                                val label = pointLabel(value.number)
                                val description = stringResource(R.string.reports_chart_description, label, formatSoles(value.income), formatSoles(value.expense))
                                Column(
                                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                        .background(if (selected == index) colors.primaryContainer.copy(alpha = 0.18f) else Color.Transparent)
                                        .selectable(selected == index, role = Role.Tab, onClick = { onSelect(index) })
                                        .semantics { contentDescription = description }.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(Modifier.height(150.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(Modifier.width(12.dp).height((150f * value.income / maximum).dp).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).background(colors.secondary))
                                        Box(Modifier.width(12.dp).height((150f * value.expense / maximum).dp).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).background(colors.tertiary))
                                    }
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun ReportCategoriesCard(report: FinancialReport) {
    val colors = MaterialTheme.colorScheme
    val tints = listOf(colors.tertiary, colors.primaryContainer, colors.secondary, colors.onSecondaryContainer, colors.outline, colors.onSurfaceVariant)
    val icons = listOf(Icons.Outlined.Restaurant, Icons.Outlined.Bolt, Icons.Outlined.ShoppingBag, Icons.Outlined.Subscriptions, Icons.Outlined.DirectionsCar, Icons.Outlined.MoreHoriz)
    ReportCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.reports_categories), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.reports_expense_total, formatSoles(report.expense)), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(stringResource(R.string.reports_category_count), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)) {
            report.categories.forEachIndexed { index, category ->
                Box(Modifier.weight(category.cents.toFloat()).fillMaxHeight().background(tints[index]))
            }
        }
        report.categories.forEachIndexed { index, category ->
            val proportion = category.cents.toFloat() / report.expense.coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = tints[index].copy(alpha = 0.15f)) {
                        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) { Icon(icons[index], null, Modifier.size(19.dp)) }
                    }
                    Text(stringResource(category.label), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatSoles(category.cents), style = MaterialTheme.typography.titleSmall)
                        Text(reportPercent(proportion * 100.0), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(colors.surfaceContainerHigh)) {
                    Box(Modifier.fillMaxWidth(proportion).fillMaxHeight().background(tints[index]))
                }
            }
        }
    }
}

@Composable
internal fun ReportChannelsCard(report: FinancialReport) {
    ReportCard {
        Text(stringResource(R.string.reports_channels), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.reports_channels_detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        report.channels.forEach { channel ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InstitutionAvatar(channel.source)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(channel.label), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.reports_channel_share, reportPercent(channel.cents * 100.0 / (report.income + report.expense))), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(formatSoles(channel.cents), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.reports_operations, channel.operations), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReportSubscriptionsCard(report: FinancialReport) {
    val total = report.categories.first { it.label == R.string.reports_category_subscriptions }.cents
    ReportCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Autorenew, null, Modifier.size(20.dp))
            Text(stringResource(R.string.reports_subscriptions), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(reportPercent(total * 100.0 / report.expense), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(formatSoles(total), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.reports_recurring_period), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(R.string.reports_active_services), style = MaterialTheme.typography.labelSmall)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            demoSubscriptions.forEach { (name, amount) ->
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Text("$name ${formatSoles(amount)}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(stringResource(R.string.reports_monthly_services), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            months.take(11).forEachIndexed { index, month ->
                DropdownMenuItem(text = { Text(month) }, onClick = { expanded = false; onSelect(index + 1) })
            }
        }
    }
}
