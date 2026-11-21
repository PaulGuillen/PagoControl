package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.ManualExpense
import com.devpaul.pagocontrol.domain.SubscriptionRule
import com.devpaul.pagocontrol.features.reports.R
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal fun expenseCents(value: String): Long? {
    if (!Regex("[0-9]{1,9}([.,][0-9]{1,2})?").matches(value.trim())) return null
    return runCatching { BigDecimal(value.trim().replace(',', '.')).movePointRight(2).longValueExact() }
        .getOrNull()?.takeIf { it in 1..99999999999L }
}

@Composable
internal fun CategoryMenu(selected: ExpenseCategory, enabled: Boolean, categories: List<ExpenseCategory> = ExpenseCategory.entries, modifier: Modifier = Modifier, onSelect: (ExpenseCategory) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .clickable(enabled = enabled, role = Role.Button, onClickLabel = stringResource(R.string.reports_category)) { expanded = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(10.dp).background(selected.chartColor(categories), CircleShape))
            Text(selected.displayName(), color = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(expanded, { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(10.dp).background(category.chartColor(categories), CircleShape))
                        Text(category.displayName())
                    }
                }, onClick = { expanded = false; onSelect(category) })
            }
        }
    }
}

@Composable
internal fun ReportExpenseDialog(state: ReportsUiState, onIntent: (ReportsUiIntent) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(state.referenceDate.toString()) }
    var channel by rememberSaveable { mutableStateOf("Efectivo") }
    var category by rememberSaveable { mutableStateOf(ExpenseCategory.Other) }
    val cents = expenseCents(amount)
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val valid = title.trim().length in 1..80 && cents != null && parsedDate != null && !parsedDate.isAfter(LocalDate.now()) && channel.trim().length in 1..40
    AlertDialog(
        onDismissRequest = { if (!state.saving) onIntent(ReportsUiIntent.Dismiss) },
        title = { Text(stringResource(R.string.reports_add_expense)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it.take(80) }, label = { Text(stringResource(R.string.reports_name)) }, singleLine = true)
                OutlinedTextField(amount, { amount = it }, label = { Text(stringResource(R.string.reports_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(date, { date = it }, label = { Text(stringResource(R.string.reports_date)) }, singleLine = true)
                OutlinedTextField(channel, { channel = it.take(40) }, label = { Text(stringResource(R.string.reports_channel)) }, singleLine = true)
                Text(stringResource(R.string.reports_category), style = MaterialTheme.typography.labelMedium)
                CategoryMenu(category, !state.saving, ExpenseCategory.entries + state.settings.customCategories) { category = it }
                if (!valid) Text(stringResource(R.string.reports_invalid_expense), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(enabled = valid && !state.saving, onClick = {
                onIntent(ReportsUiIntent.SaveExpense(ManualExpense(UUID.randomUUID().toString(), title.trim(), cents!!, parsedDate.toString(), channel.trim(), category)))
            }) { Text(stringResource(R.string.reports_save)) }
        },
        dismissButton = { TextButton(onClick = { onIntent(ReportsUiIntent.Dismiss) }, enabled = !state.saving) { Text(stringResource(R.string.reports_cancel)) } },
    )
}

@Composable
internal fun ReportSubscriptionDialog(state: ReportsUiState, onIntent: (ReportsUiIntent) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var keyword by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!state.saving) onIntent(ReportsUiIntent.Dismiss) },
        title = { Text(stringResource(R.string.reports_add_subscription)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.reports_subscription_rule_detail), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(name, { name = it.take(60) }, label = { Text(stringResource(R.string.reports_subscription_name)) }, singleLine = true)
                OutlinedTextField(keyword, { keyword = it.take(80) }, label = { Text(stringResource(R.string.reports_subscription_keyword)) }, singleLine = true)
                Text(stringResource(R.string.reports_subscription_validation), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(enabled = name.trim().length in 2..60 && keyword.trim().length in 3..80 && !state.saving, onClick = {
                onIntent(ReportsUiIntent.SaveSubscription(SubscriptionRule(UUID.randomUUID().toString(), name.trim(), keyword.trim())))
            }) { Text(stringResource(R.string.reports_save)) }
        },
        dismissButton = { TextButton(onClick = { onIntent(ReportsUiIntent.Dismiss) }, enabled = !state.saving) { Text(stringResource(R.string.reports_cancel)) } },
    )
}
