package com.devpaul.pagocontrol.features.home.domain.recognition

import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import com.devpaul.pagocontrol.features.home.domain.validation.WalletTransactionCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticBankWalletPaymentTest {
    private val notification = PaymentNotification(
        id = "plin-1", packageName = "pe.com.interbank.mobilebanking",
        source = "Interbank", title = "Interbank",
        text = "Celia Gutierrez Tafur te ha plineado S/\n0.50", postedAt = 1,
        suggestedAmountCents = null,
    )

    @Test
    fun capturesScreenshotFormatFromBankApps() {
        listOf("pe.com.interbank.mobilebanking", "pe.com.scotiabank.blpm.android.client",
            "com.bbva.nxt_peru", "com.mibanco.bancamovil").forEach { packageName ->
            val bank = notification.copy(packageName = packageName)
            assertTrue(WalletTransactionCandidate.matches(bank))
            assertEquals(AutomaticBankWalletPayment("Celia Gutierrez Tafur", 50),
                AutomaticBankWalletPayment.recognize(bank))
        }
    }

    @Test
    fun routesPlinByDirectionAndLeavesOtherBankPaymentsForReview() {
        listOf(
            "Recibiste S/ 25.00 por PLIN" to 2500L,
            "Enviaste S/ 5.00 por Plin" to -500L,
            "Pagaste S/ 12.00 con Plin" to -1200L,
        ).forEach { (text, expected) ->
            assertEquals(expected, AutomaticBankWalletPayment.recognize(notification.copy(text = text))?.amountCents)
        }
        listOf("Recibiste un abono S/ 25.00", "Pagaste S/ 12.00 con tarjeta",
            "Cobro de servicio S/ 15.00", "Ana te envio S/ 25.00").forEach { text ->
            val bank = notification.copy(text = text)
            assertTrue(text, WalletTransactionCandidate.matches(bank))
            assertNull(text, AutomaticBankWalletPayment.recognize(bank))
        }
        assertEquals(2500L, AutomaticBankWalletPayment.recognize(
            notification.copy(title = "Plin", text = "Recibiste S/ 25.00"))?.amountCents)
        assertNull(AutomaticBankWalletPayment.recognize(notification.copy(text = "Plin S/ 25.00")))
    }

    @Test
    fun rejectsUnknownSourcesAndAmbiguousOrUnsuccessfulPayments() {
        assertNull(AutomaticBankWalletPayment.recognize(notification.copy(packageName = "test.other")))
        listOf("Recuerda pagar S/ 5.00",
            "Celia te ha plineado S/ 0.50. Saldo S/ 10.00",
            "Celia te ha plineado S/ 1,000", "Celia te ha plineado S/ 0",
            "Celia te ha plineado USD 5.00").forEach { text ->
            assertNull(text, AutomaticBankWalletPayment.recognize(notification.copy(text = text)))
        }
        assertNull(AutomaticBankWalletPayment.recognize(notification.copy(title = "Pago rechazado")))
    }
}
