package com.devpaul.pagocontrol.domain

import java.text.Normalizer
import java.util.Locale

object GmailTransactionCandidate {
    const val PACKAGE_NAME = "com.google.android.gm"
    private val transactionWords = Regex("\\b(pago|pagos|pagaste|pagado|compra|compras|compraste|cobro|cobros|transferencia|transferencias|transferiste|deposito|depositos|depositaron|abono|abonos|cargo|cargos|retiro|retiros|recibiste|recibo|comprobante|payment|purchase|transfer|deposit|receipt)\\b")

    fun matches(notification: PaymentNotification): Boolean {
        if (notification.packageName != PACKAGE_NAME) return false
        val text = Normalizer.normalize("${notification.title} ${notification.text}", Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
        return transactionWords.containsMatchIn(text)
    }
}
