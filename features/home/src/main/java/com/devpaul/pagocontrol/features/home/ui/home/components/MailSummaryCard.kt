package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.domain.Dashboard

@Composable
fun MailSummaryCard(income: Boolean, dashboard: Dashboard, modifier: Modifier) {
    val tint =
        if (income) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val amount = if (income) dashboard.incomeCents else -dashboard.expenseCents
    val count = if (income) dashboard.incomeCount else dashboard.expenseCount
    PagoCard(modifier) {
        Icon(
            imageVector = if (income) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
            contentDescription = null,
            tint = tint
        )
        Text(
            stringResource(if (income) R.string.gmail_income else R.string.gmail_expense),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = formatSoles(amount, signed = true),
            color = tint,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(
                if (income) R.string.home_income_count else R.string.gmail_expense_count,
                count
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
