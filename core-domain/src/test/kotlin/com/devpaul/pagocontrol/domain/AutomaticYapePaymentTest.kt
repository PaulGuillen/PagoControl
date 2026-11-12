package com.devpaul.pagocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticYapePaymentTest {
    private val notification = PaymentNotification(
        id = "yape-1", packageName = "com.bcp.innovacxion.yapeapp", source = "Yape",
        title = "Confirmación de Pago", text = "Celia Gut* te envió un pago por S/ 1. El cód. de seguridad es: 854",
        postedAt = 1, suggestedAmountCents = 100,
    )

    @Test fun recognizesRealDeviceFormatAndExtractsSenderAndSoles() {
        assertEquals(AutomaticYapePayment(sender = "Celia Gut*", amountCents = 100), AutomaticYapePayment.recognize(notification))
        assertEquals(2500L, AutomaticYapePayment.recognize(notification.copy(
            text = "María te envió un pago por S/ 25.00",
        ))?.amountCents)
        assertEquals(123456L, AutomaticYapePayment.recognize(notification.copy(
            text = "María te envió un pago por S/ 1,234.56. El cód. de seguridad es: 123",
        ))?.amountCents)
    }

    @Test fun doesNotTrustLabelsOrAmountsSuggestedByAnotherSource() {
        assertNull(AutomaticYapePayment.recognize(notification.copy(packageName = "test.other")))
        assertNull(AutomaticYapePayment.recognize(notification.copy(packageName = "com.google.android.gm")))
        assertNull(AutomaticYapePayment.recognize(notification.copy(title = "Promoción")))
        assertEquals(100L, AutomaticYapePayment.recognize(notification.copy(suggestedAmountCents = 99999))?.amountCents)
    }

    @Test fun keepsRemindersFailuresOutgoingAndAmbiguousAmountsForReview() {
        listOf(
            "Enviaste un pago por S/ 1",
            "Tu pago por S/ 1 fue rechazado",
            "Recuerda pagar S/ 1",
            "María te envió un pago por S/ 1. Saldo S/ 25",
            "María te envió un pago por USD 1",
            "María te envió un pago por S/ 1,000",
            "María te envió un pago por S/ 0",
        ).forEach { text ->
            assertNull(text, AutomaticYapePayment.recognize(notification.copy(text = text)))
        }
    }
}
