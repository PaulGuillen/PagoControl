package com.devpaul.pagocontrol.features.home.ui.home

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
    PagoCard(Modifier.fillMaxWidth()) {
        Text(text = "${notification.source} · $date", style = MaterialTheme.typography.labelMedium)
        Text(notification.title, style = MaterialTheme.typography.titleSmall)
        Text(notification.text, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(enabled = !saving, onClick = { reviewing = true }) { Text(stringResource(R.string.notifications_review)) }
            TextButton(enabled = !saving, onClick = { onIntent(HomeUiIntent.DiscardNotification(id = notification.id, mail = mail)) }) {
                Text(stringResource(R.string.notifications_discard))
            }
        }
    }
    if (reviewing) AlertDialog(
        onDismissRequest = { reviewing = false },
        title = { Text(stringResource(R.string.notifications_review)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (mail) R.string.gmail_review_hint else R.string.notifications_review_hint))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.notifications_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amount.isNotBlank() && PaymentAmount.parse(amount) == null,
                )
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
