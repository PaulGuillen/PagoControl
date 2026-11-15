package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.features.home.domain.entity.Dashboard

@Composable
fun MailBalanceCard(dashboard: Dashboard) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.gmail_balance),
                style = MaterialTheme.typography.labelLarge
            )
            Text(formatSoles(dashboard.balanceCents), style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(R.string.gmail_balance_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
