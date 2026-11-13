package com.devpaul.pagocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticBcpExpenseTest {
    private val notification = PaymentNotification(
        id = "bcp-consumption", packageName = GmailTransactionCandidate.PACKAGE_NAME, source = "Gmail",
        title = "BCP Notificaciones", postedAt = 1, suggestedAmountCents = null,
        text = """
            Realizaste un consumo con tu Tarjeta de Débito BCP - Servicio de Notificaciones BCP
            Hola Paul Antony,
            Realizaste un consumo de S/ 25.78 con tu Tarjeta de Débito BCP en WL *STEAM PURCHASE.
            Por tu seguridad, te enviamos los datos de tu operación.
            Monto
            Total del consumo
            S/ 25.78
            Datos de la operación
            Operación realizada
            Consumo Tarjeta de Débito
            Fecha y hora
            12 de noviembre de 2026 - 04:41 AM
            Número de Tarjeta de Débito
            ************8136
            Empresa
            WL *STEAM PURCHASE
            Número de operación
            401766
        """.trimIndent(),
    )

    @Test fun recognizesScreenshotWithRepeatedAmountAsExpense() {
        assertEquals(
            AutomaticBcpExpense(merchant = "WL *STEAM PURCHASE", amountCents = -2578),
            AutomaticBcpExpense.recognize(notification),
        )
        assertEquals(-2578L, AutomaticBcpExpense.recognize(notification.copy(suggestedAmountCents = 99999))?.amountCents)
    }

    @Test fun merchantTitleStopsBeforeBcpReceiptDetailsWithoutSecurityIntroduction() {
        val text = "Realizaste un consumo de S/ 6.56 con tu Tarjeta de D\u00e9bito BCP en WL *STEAM PURCHASE.\u00a0\nMonto\nTotal del consumo\nS/ 6.56\nDatos de la operaci\u00f3n\nEmpresa\nWL *STEAM PURCHASE"
        assertEquals(AutomaticBcpExpense("WL *STEAM PURCHASE", -656), AutomaticBcpExpense.recognize(notification.copy(text = text)))
        assertEquals("WL *STEAM PURCHASE", AutomaticGmailPayment.recognize(notification.copy(text = text))?.title)
    }

    @Test fun recognizesConsumptionWithoutEnglishTransactionWords() {
        val consumption = notification.copy(text = "Realizaste un consumo de S/ 10.00 con tu Tarjeta de Débito BCP en TIENDA.")
        assertTrue(GmailTransactionCandidate.matches(consumption))
        assertEquals(AutomaticBcpExpense("TIENDA", -1000), AutomaticBcpExpense.recognize(consumption))
        assertTrue(GmailTransactionCandidate.matches(consumption.copy(text = "Consumo tarjeta de débito S/ 10.00")))
    }

    @Test fun leavesUnknownSourcesAndAmbiguousOrUnsuccessfulMessagesForReview() {
        assertNull(AutomaticBcpExpense.recognize(notification.copy(packageName = "other.app")))
        assertNull(AutomaticBcpExpense.recognize(notification.copy(title = "Otro banco")))
        listOf(
            notification.text.replace("S/ 25.78\n", "S/ 30.00\n"),
            notification.text.replace("S/ 25.78", "USD 25.78"),
            notification.text.replace("S/ 25.78", "S/ 0"),
            notification.text.replace("S/ 25.78", "S/ 1,000"),
            notification.text.replace("Realizaste un consumo de", "No realizaste un consumo de"),
            notification.text + "\nConsumo anulado",
            "Tu consumo de S/ 25.78 fue rechazado",
            "Recuerda pagar S/ 25.78",
            "Transferencia recibida S/ 25.78",
        ).forEach { text ->
            assertNull(text, AutomaticBcpExpense.recognize(notification.copy(text = text)))
        }
    }
}
