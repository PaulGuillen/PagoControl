package com.devpaul.pagocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticGmailPaymentTest {
    private fun notification(text: String) = PaymentNotification(
        id = "mail", packageName = GmailTransactionCandidate.PACKAGE_NAME, source = "Gmail",
        title = "Banco", text = text, postedAt = 1, suggestedAmountCents = null,
    )

    @Test fun automaticallyClassifiesIncomeAndExpensesFromDifferentSenders() {
        listOf("Transferencia recibida S/ 100.00", "Recibiste un pago S/ 100.00", "Abono PEN 100.00", "Payment received S/ 100.00", "María te envió un pago por S/ 100.00")
            .forEach { assertEquals(it, 10000L, AutomaticGmailPayment.recognize(notification(it))?.amountCents) }
        listOf("Pago realizado S/ 25.78", "Transferencia enviada S/ 25.78", "Compra aprobada S/ 25.78", "Retiro PEN 25.78")
            .forEach { assertEquals(it, -2578L, AutomaticGmailPayment.recognize(notification(it))?.amountCents) }
    }

    @Test fun acceptsRepeatedAmountsAndUsesTransactionAmountBeforeBalance() {
        assertEquals(-2578L, AutomaticGmailPayment.recognize(notification("Compra S/ 25.78. Total S/ 25.78"))?.amountCents)
        assertEquals(10000L, AutomaticGmailPayment.recognize(notification("Transferencia recibida S/ 100.00. Saldo S/ 500.00"))?.amountCents)
    }

    @Test fun doesNotInventAmountsOrRegisterPromotionsAndFailedPayments() {
        listOf("Recibiste un pago", "Pago USD 25.00", "Pago rechazado S/ 25.00", "Recuerda pagar S/ 25.00", "Oferta de compra S/ 25.00", "Reunión S/ 25.00")
            .forEach { assertNull(it, AutomaticGmailPayment.recognize(notification(it))) }
        assertNull(AutomaticGmailPayment.recognize(notification("Pago S/ 25.00").copy(packageName = "other.app")))
    }
}
