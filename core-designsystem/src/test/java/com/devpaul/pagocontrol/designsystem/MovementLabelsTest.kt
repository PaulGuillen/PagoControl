package com.devpaul.pagocontrol.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class MovementLabelsTest {
    @Test
    fun presentsExistingServicePaymentWithYapeBadge() {
        val labels = movementLabels("Pago", "Gmail", "com.google.android.gm", "YAPE Notificaciones",
            "Tu yapeo de servicio ha sido confirmado\nHola, PAUL\n¡Tu pago fue realizado!\nMonto pagado\nS/ 282.90\nYapero(a)\nPAUL ANTONY GUILLEN ACUNA\nDetalle del servicio")
        assertEquals("Pago de servicio", labels.title)
        assertEquals("Yape", labels.institution)
        assertEquals(true, labels.fromGmail)
    }

    @Test
    fun improvesGenericBankPaymentWithoutAssumingAService() {
        assertEquals("Pago de servicio", mail("BCP", "Pago de un servicio confirmado\nS/ 84.90", "Pago").title)
        assertEquals("Compra", mail("BCP", "Compra aprobada S/ 84.90", "Pago").title)
        assertEquals("Pago", mail("BCP", "Pago realizado S/ 84.90", "Pago").title)
        assertEquals("EBN*CRUNCHYROLL", mail("BCP", "Compra aprobada", "BCP - EBN*CRUNCHYROLL").title)
    }

    private fun mail(bank: String, text: String, title: String = "$bank Notificaciones") =
        movementLabels(title, "Gmail", "com.google.android.gm", "$bank Notificaciones", text)

    @Test
    fun extractsRecipientForPlinAcrossBanks() {
        listOf("Interbank", "BCP", "Scotiabank", "BBVA").forEach { bank ->
            val labels = mail(bank, "Constancia de Pago Plin\nDestinatario\nJHOSSY JHOSSEP VARGAS SALDA\u00d1A\nDestino\nPlin\nMonto y moneda\nS/ 1.00")
            assertEquals(bank, labels.institution)
            assertEquals("Plin \u00b7 JHOSSY JHOSSEP", labels.title)
        }
    }

    @Test
    fun usesExplicitMerchantOrBeneficiaryFields() {
        assertEquals("TIENDA UNO", mail("BBVA", "Compra realizada\nComercio: TIENDA UNO\nMonto\nS/ 25.00").title)
        assertEquals("Ana Maria", mail("Scotiabank", "Transferencia enviada\nBeneficiario\nAna Maria Perez\nImporte\nS/ 10.00").title)
    }

    @Test
    fun keepsBcpMerchantAndItsPunctuation() {
        assertEquals("STEAMGAMES.COM", mail("BCP", "", "BCP - STEAMGAMES.COM").title)
        assertEquals("DLC*Temu.com", mail("BCP", "Realizaste un consumo de S/ 20.00 con tu Tarjeta de Debito BCP en DLC*Temu.com. Por tu seguridad revisa tu cuenta.").title)
    }

    @Test
    fun suppliesUsefulFallbacksWithoutInventingPeople() {
        listOf("Interbank", "BCP", "Scotiabank", "BBVA").forEach { bank ->
            assertEquals("Compra", mail(bank, "Compra aprobada S/ 25.00").title)
            assertEquals("Transferencia", mail(bank, "Transferencia recibida S/ 10.00").title)
            assertEquals("Pago Plin", mail(bank, "Constancia de Pago Plin S/ 10.00").title)
            assertEquals("Retiro", mail(bank, "Retiro S/ 10.00").title)
            assertEquals("Pago Plin", mail(bank, "Pago Plin\nDestinatario\n1234567890\nDestino\nPlin").title)
        }
    }

    @Test
    fun walletPlinUsesBadgeAndOnlyPersonName() {
        listOf("Interbank", "BBVA", "Scotiabank").forEach { bank ->
            val labels = movementLabels("Celia Gutierrez Tafur", bank, "bank.app", bank,
                "Celia Gutierrez Tafur te ha plineado S/ 1.00")
            assertEquals("Plin", labels.institution)
            assertEquals("Celia Gutierrez", labels.title)
        }
        val prefixed = movementLabels("PLIN - Celia Gutierrez Tafur", "Interbank", "bank.app", "Plin", "Recibiste S/ 1.00 por Plin")
        assertEquals("Celia Gutierrez", prefixed.title)
        assertEquals("Plin", prefixed.institution)
        assertEquals("Interbank", movementLabels("Compra", "Interbank", "bank.app", "Compra", "Compra S/ 25.00").institution)
    }

    @Test
    fun preservesWalletNamesAndUnknownSources() {
        assertEquals("Celia Gutierrez", movementLabels("Celia Gutierrez Tafur", "Yape", "yape", "Confirmacion", "").title)
        val unknown = movementLabels("Tu recibo", "Gmail", "com.google.android.gm", "Tu recibo", "Gracias")
        assertEquals("Tu recibo", unknown.title)
        assertEquals("Gmail", unknown.institution)
    }
}
