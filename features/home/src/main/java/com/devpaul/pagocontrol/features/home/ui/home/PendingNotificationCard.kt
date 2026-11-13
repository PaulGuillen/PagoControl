package com.devpaul.pagocontrol.features.home.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.devpaul.pagocontrol.designsystem.formatSoles
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.domain.PaymentAmount
import com.devpaul.pagocontrol.domain.PaymentNotification
import com.devpaul.pagocontrol.features.home.R
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun PendingNotificationCard(notification: PaymentNotification, saving: Boolean, onIntent: (HomeUiIntent) -> Unit, mail: Boolean = false) {
    var reviewing by rememberSaveable(notification.id) { mutableStateOf(false) }
    var amount by rememberSaveable(notification.id) {
        mutableStateOf(notification.suggestedAmountCents?.let { BigDecimal.valueOf(it, 2).toPlainString() }.orEmpty())
    }
    val date = remember(notification.postedAt) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("es-PE")).format(Date(notification.postedAt))
    }
        PagoCard(Modifier.fillMaxWidth().clickable(enabled = !saving, role = Role.Button) { reviewing = true }) {
            Text(notification.title.ifBlank { notification.source }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${notification.source} \u00b7 $date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(notification.suggestedAmountCents?.let { formatSoles(it) } ?: stringResource(R.string.gmail_review_no_amount), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(if (mail && notification.possibleDuplicate) R.string.gmail_duplicate_short else R.string.gmail_review_detail), color = if (mail && notification.possibleDuplicate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    if (reviewing) AlertDialog(
        onDismissRequest = { if (!saving) reviewing = false },
        title = { Text(stringResource(R.string.notifications_review)) },
        text = {
            Column(modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                run {
                    Text(notification.title.ifBlank { notification.source }, style = MaterialTheme.typography.titleMedium)
                    Text("${notification.source} \u00b7 $date", style = MaterialTheme.typography.labelMedium)
                }
                if (mail && notification.possibleDuplicate) Text(stringResource(R.string.gmail_duplicate_hint))
                run {
                    Text(stringResource(if (mail) R.string.gmail_review_message else R.string.notifications_review_message), style = MaterialTheme.typography.labelLarge)
                    val message = remember(notification.text) {
                        notification.text.lineSequence().map { it.trim().replace(Regex("[\\t ]+"), " ") }
                            .filter { it.isNotBlank() }.joinToString("\n\n")
                    }
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
                Text(stringResource(if (mail) R.string.gmail_review_brief else R.string.notifications_review_hint))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.notifications_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amount.isNotBlank() && PaymentAmount.parse(amount) == null,
                )
                TextButton(enabled = !saving, onClick = {
                    onIntent(HomeUiIntent.DiscardNotification(id = notification.id, mail = mail))
                    reviewing = false
                }) { Text(stringResource(if (mail && notification.possibleDuplicate) R.string.gmail_already_recorded else R.string.notifications_discard)) }
                val cents = PaymentAmount.parse(amount)
                Button(enabled = cents != null && !saving, onClick = {
                    cents?.let { onIntent(HomeUiIntent.ConfirmPayment(id = notification.id, amountCents = it, mail = mail)) }
                    reviewing = false
                }) { Text(stringResource(R.string.notifications_income)) }
                if (mail) Button(enabled = cents != null && !saving, onClick = {
                    cents?.let { onIntent(HomeUiIntent.ConfirmPayment(id = notification.id, amountCents = -it, mail = true)) }
                    reviewing = false
                }) { Text(stringResource(R.string.gmail_confirm_expense)) }
            }
        },
        confirmButton = { TextButton(onClick = { reviewing = false }) { Text(stringResource(R.string.notifications_cancel)) } },
    )
}
