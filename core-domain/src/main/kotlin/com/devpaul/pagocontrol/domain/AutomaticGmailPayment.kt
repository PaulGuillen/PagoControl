package com.devpaul.pagocontrol.domain

import java.text.Normalizer
import java.util.Locale

data class AutomaticGmailPayment(val title: String, val amountCents: Long) {
    companion object {
        private val amounts = Regex("(?:S/\\.?|PEN)\\s*([0-9]+(?:[.,][0-9]+)*)", RegexOption.IGNORE_CASE)
        private val income = Regex("\\b(recibiste|recibido|recibida|recibidos|depositaron|abonaron|abonado|abono|ingreso|received|credited|refund|reembolso|devolucion|te envio)\\b")
        private val outgoing = Regex("\\b(enviaste|enviada|enviado|transferiste|pagaste|consumo|compra|retiro|cargo|purchase|sent|withdrawal)\\b")
        private val excluded = Regex("\\b(rechazado|rechazada|fallido|fallida|anulado|anulada|cancelado|cancelada|recordatorio|recuerda|promocion|oferta|descuento|failed|declined|reminder)\\b")

        fun recognize(notification: PaymentNotification): AutomaticGmailPayment? {
            if (!GmailTransactionCandidate.matches(notification)) return null
            val normalized = Normalizer.normalize("${notification.title}\n${notification.text}", Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
            if (excluded.containsMatchIn(normalized)) return null
            AutomaticBcpExpense.recognize(notification)?.let {
                return AutomaticGmailPayment(it.merchant, it.amountCents)
            }
            val amount = amounts.find(notification.text)?.let { PaymentAmount.parse(it.groupValues[1]) }
                ?: amounts.find(notification.title)?.let { PaymentAmount.parse(it.groupValues[1]) }
                ?: return null
            val direction = if (income.containsMatchIn(normalized) && !outgoing.containsMatchIn(normalized)) 1 else -1
            return AutomaticGmailPayment(notification.title.ifBlank { notification.source }, direction * amount)
        }
    }
}
