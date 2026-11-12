package com.devpaul.pagocontrol.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GmailTransactionCandidateTest {
    private val notification = PaymentNotification(
        id = "gmail", packageName = GmailTransactionCandidate.PACKAGE_NAME, source = "Gmail",
        title = "Banco", text = "Confirmación de transferencia por S/ 100.00", postedAt = 1, suggestedAmountCents = 10000,
    )

    @Test fun identifiesCandidatesEvenWhenTheAmountIsTruncated() {
        listOf("Pago realizado S/ 50.00", "Recibiste un depósito", "Compra aprobada", "Comprobante de transferencia", "Payment received")
            .forEach { text -> assertTrue(text, GmailTransactionCandidate.matches(notification.copy(text = text))) }
    }

    @Test fun ignoresUnrelatedMailAndOtherApps() {
        assertFalse(GmailTransactionCandidate.matches(notification.copy(text = "Reunión del equipo mañana")))
        assertFalse(GmailTransactionCandidate.matches(notification.copy(packageName = "test.other")))
    }
}
