package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.features.home.domain.entity.Dashboard
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import java.util.Calendar
import kotlin.math.abs

@Composable
fun FlowCard(
    dashboard: Dashboard,
    monthly: Boolean,
    selected: Int,
    onIntent: (HomeUiIntent) -> Unit,
    mail: Boolean = false
) {
    val values = if (monthly) dashboard.monthlyIncomeFlow else dashboard.weeklyIncomeFlow
    val netValues = if (monthly) dashboard.monthlyFlow else dashboard.weeklyFlow
    val expenses = values.mapIndexed { index, income ->
        (income - netValues.getOrElse(index) { 0L }).coerceAtLeast(0)
    }
    val incomeLabel = stringResource(R.string.gmail_income)
    val expenseLabel = stringResource(R.string.gmail_expense)
    val labels = stringArrayResource(if (monthly) R.array.home_months else R.array.home_days)
    val monthScroll = rememberScrollState()
    val monthCellWidth = with(LocalDensity.current) { 56.dp.roundToPx() }
    LaunchedEffect(monthly, selected) {
        if (monthly) monthScroll.animateScrollTo((selected - 2).coerceAtLeast(0) * monthCellWidth)
    }
    PagoCard(Modifier.fillMaxWidth()) {
        Text(
            stringResource(if (mail) R.string.gmail_flow else R.string.home_flow),
            style = MaterialTheme.typography.headlineSmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(false, true).forEach { isMonthly ->
                Surface(
                    color = if (monthly == isMonthly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (monthly == isMonthly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.semantics { this.selected = monthly == isMonthly }
                        .clickable(role = Role.Tab) { onIntent(HomeUiIntent.SelectPeriod(isMonthly)) },
                ) {
                    Text(
                        text = stringResource(if (isMonthly) R.string.home_month else R.string.home_week),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        if (monthly) Text(
            stringResource(
                R.string.home_chart_year,
                Calendar.getInstance().get(Calendar.YEAR)
            ), style = MaterialTheme.typography.labelMedium
        )
        if (mail) {
            Text(labels.getOrElse(selected) { "" }, style = MaterialTheme.typography.labelMedium)
            Text(
                "$incomeLabel: ${formatSoles(values.getOrElse(selected) { 0L })}",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "$expenseLabel: ${formatSoles(expenses.getOrElse(selected) { 0L })}",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            Text(
                text = "${labels.getOrElse(selected) { "" }} \u00b7 ${
                    formatSoles(
                        values.getOrElse(
                            selected
                        ) { 0 }, signed = true
                    )
                }",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Row(
            modifier = if (monthly) Modifier.fillMaxWidth()
                .horizontalScroll(monthScroll) else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val maximum =
                ((values + if (mail) expenses else emptyList()).maxOrNull() ?: 1L).coerceAtLeast(1L)
            values.forEachIndexed { index, amount ->
                val label = labels.getOrElse(index) { "" }
                val expense = expenses.getOrElse(index) { 0L }
                val description =
                    if (mail) "$label: $incomeLabel ${formatSoles(amount)}, $expenseLabel ${
                        formatSoles(expense)
                    }" else "$label: ${formatSoles(amount)}"
                Column(
                    modifier = (if (monthly) Modifier.width(48.dp) else Modifier.weight(1f)).clip(
                        MaterialTheme.shapes.small
                    )
                        .clickable(role = Role.Button) { onIntent(HomeUiIntent.SelectBar(index)) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = description
                            this.selected = selected == index
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (mail) {
                        Row(
                            Modifier.fillMaxWidth().height(120.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                Modifier.weight(1f).height((120 * amount.toFloat() / maximum).dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Box(
                                Modifier.weight(1f).height((120 * expense.toFloat() / maximum).dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.tertiary)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                Modifier.fillMaxWidth(0.8f)
                                    .height((24 + 96 * abs(amount).toFloat() / maximum).dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
                            )
                        }
                    }
                    Text(
                        label, style = MaterialTheme.typography.labelSmall,
                        color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
