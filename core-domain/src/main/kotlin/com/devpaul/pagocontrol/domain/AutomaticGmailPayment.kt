package com.devpaul.pagocontrol.domain

import java.text.Normalizer
import java.util.Locale

data class AutomaticGmailPayment(val title: String, val amountCents: Long) {
    companion object {
        // Notification titles are only a recognition hint, not authenticated email senders.
        private val institutions = Regex("\\b(bcp|interbank|scotiabank|bbva|banbif|mibanco|pichincha|gnb|alfin|agrobanco|banco de la nacion|banco de comercio|banco falabella|banco ripley|banco santander|compartamos|financiera efectiva|financiera oh|caja (?:municipal |rural |de ahorro y credito )?(?:arequipa|huancayo|piura|sullana|cusco|trujillo|ica|tacna|maynas|paita|del santa|los andes|prymera|incasur|cencosud|metropolitana))\\b")
        private fun normalized(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)

        fun isCandidate(notification: PaymentNotification): Boolean =
            GmailTransactionCandidate.matches(notification) &&
                !excluded.containsMatchIn(normalized("${notification.title} ${notification.text}"))

        fun suggestedAmount(notification: PaymentNotification): Long? =
            amounts.findAll("${notification.title} ${notification.text}")
                .mapNotNull { PaymentAmount.parse(it.groupValues[1]) }.distinct().toList().singleOrNull()

        fun possibleDuplicate(first: PaymentNotification, second: PaymentNotification, recordedAmount: Long? = null): Boolean {
            val amount = suggestedAmount(first) ?: return false
            if (amount != (recordedAmount?.let { kotlin.math.abs(it) } ?: suggestedAmount(second))) return false
            if (kotlin.math.abs(first.postedAt - second.postedAt) > 24 * 60 * 60 * 1000L) return false
            val firstDirection = direction(first)
            val secondDirection = recordedAmount?.compareTo(0L) ?: direction(second)
            return firstDirection == null || secondDirection == null || firstDirection == secondDirection
        }

        private fun direction(notification: PaymentNotification): Int? {
            val text = normalized("${notification.title} ${notification.text}")
            val incoming = income.containsMatchIn(text)
            val outgoing = outgoing.containsMatchIn(text)
            return when {
                incoming && !outgoing -> 1
                outgoing && !incoming -> -1
                else -> null
            }
        }

        private val amounts = Regex("(?:S/\\.?|PEN)\\s*([0-9]+(?:[.,][0-9]+)*)", RegexOption.IGNORE_CASE)
        private val income = Regex("\\b(transferencia recibida|recibiste|recibido|recibida|recibidos|depositaron|abonaron|abonado|abono|ingreso|received|credited|refund|reembolso|devolucion|te envio)\\b")
        private val outgoing = Regex("\\b(pago realizado|compra realizada|enviaste|enviada|enviado|transferiste|pagaste|consumo|compra|retiro|cargo|purchase|sent|withdrawal)\\b")
        private val excluded = Regex("\\b(no realizaste|pendiente|solicitud|rechazado|rechazada|fallido|fallida|anulado|anulada|cancelado|cancelada|recordatorio|recuerda|promocion|oferta|descuento|failed|declined|reminder)\\b")

        fun recognize(notification: PaymentNotification): AutomaticGmailPayment? {
            if (!isCandidate(notification)) return null
            if (!institutions.containsMatchIn(normalized(notification.title))) return null
            val normalized = Normalizer.normalize("${notification.title}\n${notification.text}", Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
            if (excluded.containsMatchIn(normalized)) return null
            AutomaticBcpExpense.recognize(notification)?.let {
                return AutomaticGmailPayment(it.merchant, it.amountCents)
            }
            val amount = suggestedAmount(notification) ?: return null
            val direction = direction(notification) ?: return null
            return AutomaticGmailPayment(notification.title.ifBlank { notification.source }, direction * amount)
        }
    }
}
