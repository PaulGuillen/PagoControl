package com.devpaul.pagocontrol.domain

import org.junit.Assert.*
import org.junit.Test

class WalletTransactionCandidateTest {
    private fun notification(text: String, title: String = "Yape") = PaymentNotification(
        "test", "com.bcp.innovacxion.yapeapp", "Yape", title, text, 1, 9999,
    )

    @Test fun requiresAnExplicitValidAmountAndAnOperation() {
        listOf("Te enviaron un mensaje", "Pago recibido", "Codigo 123456", "Pago S/ 0", "Saldo disponible S/ 100.00", "Pago USD 10.00").forEach {
            assertFalse(it, WalletTransactionCandidate.matches(notification(it)))
        }
        assertTrue(WalletTransactionCandidate.matches(notification("Recibiste un pago S/ 25.00")))
        assertTrue(WalletTransactionCandidate.matches(notification("PEN 10.00", "Transferencia recibida")))
        assertFalse(WalletTransactionCandidate.matches(notification("Pago S/ 25.00").copy(packageName = GmailTransactionCandidate.PACKAGE_NAME)))
    }

    @Test fun ignoresAdvertisingRemindersAndUnsuccessfulPaymentsEvenWithAmounts() {
        listOf("Oferta: compra por S/ 20.00", "Recuerda tu pago S/ 10.00", "Compra rechazada S/ 5.00", "Pago pendiente S/ 8.00", "Gana S/ 20.00 con tu pago", "Solicitud de pago S/ 10.00").forEach {
            assertFalse(it, WalletTransactionCandidate.matches(notification(it)))
        }
    }

    @Test fun repeatedAmountsAreSuggestedButConflictingAmountsNeedReview() {
        assertEquals(2500L, WalletTransactionCandidate.suggestedAmount(notification("Pago S/ 25.00. Total S/ 25.00")))
        val multiple = notification("Pago S/ 25.00. Saldo S/ 100.00")
        assertTrue(WalletTransactionCandidate.matches(multiple))
        assertNull(WalletTransactionCandidate.suggestedAmount(multiple))
    }
}
