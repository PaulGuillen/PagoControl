package com.devpaul.pagocontrol.domain

import java.text.Normalizer
import java.util.Locale

object WalletTransactionCandidate {
    private val amounts = Regex("(?:S/\\.?|PEN)\\s*([0-9]+(?:[.,][0-9]+)*)", RegexOption.IGNORE_CASE)
    private val operations = Regex("\\b(pago|pagos|pagaste|pagado|compra|consumo|transferencia|transferiste|deposito|abono|retiro|recibiste|recibido|recibida|enviaste|enviado|enviada|te envio|yapeaste|yapeo|payment|received|purchase|transfer)\\b")
    private val excluded = Regex("\\b(promocion|promociones|oferta|ofertas|descuento|descuentos|gana|ganate|cashback|sorteo|sorteos|recordatorio|recuerda|pendiente|solicitud|solicita|rechazado|rechazada|fallido|fallida|anulado|anulada|cancelado|cancelada|failed|declined|reminder)\\b")

    fun suggestedAmount(notification: PaymentNotification): Long? =
        amounts.findAll("${notification.title} ${notification.text}")
            .mapNotNull { PaymentAmount.parse(it.groupValues[1]) }.distinct().toList().singleOrNull()

    fun matches(notification: PaymentNotification): Boolean {
        if (notification.packageName == GmailTransactionCandidate.PACKAGE_NAME) return false
        val text = Normalizer.normalize("${notification.title} ${notification.text}", Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
        return operations.containsMatchIn(text) && !excluded.containsMatchIn(text) &&
            amounts.findAll("${notification.title} ${notification.text}").any { PaymentAmount.parse(it.groupValues[1]) != null }
    }
}
