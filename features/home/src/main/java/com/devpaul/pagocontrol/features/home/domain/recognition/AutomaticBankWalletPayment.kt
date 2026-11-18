package com.devpaul.pagocontrol.features.home.domain.recognition

import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import com.devpaul.pagocontrol.features.home.domain.validation.WalletTransactionCandidate
import java.text.Normalizer
import java.util.Locale

data class AutomaticBankWalletPayment(val sender: String, val amountCents: Long) {
    companion object {
        private val packages = setOf(
            "pe.com.interbank.mobilebanking",
            "pe.com.scotiabank.blpm.android.client",
            "com.bbva.nxt_peru",
            "com.mibanco.bancamovil",
        )
        private val plin = Regex("\\bplin(?:eado|easte|eo|o|aste)?\\b")
        private val income = Regex("\\b(te (?:ha )?(?:plineado|plineo|plino|envio)|recibiste|recibido|recibida|recepcion|abono|deposito)\\b")
        private val expense = Regex("\\b(enviaste|enviada|enviado|pagaste|realizaste|plineaste|plinaste|compra|consumo)\\b")
        private val namedSender = Regex(
            "^(.+?)\\s+te\\s+(?:ha\\s+)?(?:plineado|plin(?:e)?[o\\u00f3]|envi[o\\u00f3])\\b",
            RegexOption.IGNORE_CASE,
        )

        fun recognize(notification: PaymentNotification): AutomaticBankWalletPayment? {
            if (notification.packageName !in packages || !WalletTransactionCandidate.matches(notification)) return null
            val text = Normalizer.normalize("${notification.title} ${notification.text}", Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
                .replace(Regex("\\s+"), " ")
            if (!plin.containsMatchIn(text)) return null
            val amount = WalletTransactionCandidate.suggestedAmount(notification) ?: return null
            val incoming = income.containsMatchIn(text)
            val outgoing = expense.containsMatchIn(text)
            if (incoming == outgoing) return null
            val sender = namedSender.find(notification.text.trim())?.groupValues?.get(1)
                ?: notification.title.ifBlank { notification.source }
            return AutomaticBankWalletPayment(sender = sender, amountCents = if (outgoing) -amount else amount)
        }
    }
}
