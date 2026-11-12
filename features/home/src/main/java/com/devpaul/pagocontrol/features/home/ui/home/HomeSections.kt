package com.devpaul.pagocontrol.features.home.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.designsystem.PagoCard

@Composable
internal fun HomeSectionTabs(section: HomeSection, onIntent: (HomeUiIntent) -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HomeSection.entries.forEach { tab ->
                val selected = section == tab
                Surface(
                    modifier = Modifier.weight(1f).selectable(selected = selected, role = Role.Tab) {
                        onIntent(HomeUiIntent.SelectSection(tab))
                    },
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(imageVector = if (tab == HomeSection.Wallets) Icons.Outlined.AccountBalanceWallet else Icons.Outlined.MailOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(stringResource(if (tab == HomeSection.Wallets) R.string.home_wallets_tab else R.string.home_mail_tab), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun MailTransactionsCard() {
    PagoCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.home_mail_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.home_mail_description), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.home_mail_status), style = MaterialTheme.typography.labelLarge)
    }
}
