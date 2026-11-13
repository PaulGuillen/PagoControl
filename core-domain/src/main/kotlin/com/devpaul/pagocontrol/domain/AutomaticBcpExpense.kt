package com.devpaul.pagocontrol.domain

data class AutomaticBcpExpense(val merchant: String, val amountCents: Long) {
    companion object {
        private val consumption = Regex(
            "(?:^|[.!?]\\s+|\\n)Realizaste un consumo de (?:S/\\.?|PEN)\\s*([0-9]+(?:[.,][0-9]+)*)\\s+con tu Tarjeta de D[eé]bito BCP en (.+?)(?:\\.?\\s+Por tu seguridad|\\.?$)",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        private val amounts = Regex("(?:S/\\.?|PEN)\\s*([0-9]+(?:[.,][0-9]+)*)", RegexOption.IGNORE_CASE)
        private val reversal = Regex("\\b(rechazad[oa]|anulad[oa]|cancelad[oa]|extorno|reverso)\\b", RegexOption.IGNORE_CASE)

        fun recognize(notification: PaymentNotification): AutomaticBcpExpense? {
            if (notification.packageName != GmailTransactionCandidate.PACKAGE_NAME) return null
            if (!notification.title.trim().equals("BCP Notificaciones", ignoreCase = true)) return null
            if (reversal.containsMatchIn(notification.text)) return null
            val match = consumption.find(notification.text) ?: return null
            val amount = PaymentAmount.parse(match.groupValues[1]) ?: return null
            if (amounts.findAll(notification.text).any { PaymentAmount.parse(it.groupValues[1]) != amount }) return null
            val merchant = match.groupValues[2].lineSequence().first().trim().removeSuffix(".").trim()
            if (merchant.isBlank()) return null
            return AutomaticBcpExpense(merchant = merchant, amountCents = -amount)
        }
    }
}
