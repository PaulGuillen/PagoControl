package com.devpaul.pagocontrol.domain

import java.text.Normalizer
import java.util.Locale

data class AutomaticYapePayment(val sender: String, val amountCents: Long) {
    companion object {
        private val receivedPayment = Regex(
            "^(.+?) te envio un pago por (?:s/\\.?|pen)\\s*([0-9]+(?:[.,][0-9]+)*)(?:\\.\\s*el cod\\. de seguridad es:\\s*[0-9]{3,8})?\\.?$",
        )

        fun recognize(notification: PaymentNotification): AutomaticYapePayment? {
            if (notification.packageName != "com.bcp.innovacxion.yapeapp") return null
            if (normalize(notification.title) != "confirmacion de pago") return null
            val match = receivedPayment.matchEntire(normalize(notification.text)) ?: return null
            val amount = PaymentAmount.parse(match.groupValues[2]) ?: return null
            val sender = notification.text.trim().split(Regex("\\s+te\\s+envi[oó]\\s+", RegexOption.IGNORE_CASE)).first()
            return AutomaticYapePayment(sender = sender, amountCents = amount)
        }

        private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
