package com.devpaul.pagocontrol.features.home.domain.recognition

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnAccountTransferTest {
    @Test
    fun requiresExplicitOwnAccountPhrase() {
        assertTrue(
            OwnAccountTransfer.matches(
                title = "BCP",
                text = "Transferencia entre mis cuentas S/ 100.00"
            )
        )
        assertTrue(
            OwnAccountTransfer.matches(
                title = "TRANSFERENCIA ENTRE MIS CUENTAS",
                text = "S/ 100.00"
            )
        )
        assertFalse(OwnAccountTransfer.matches(title = "BCP", text = "Compra OPENAI S/ 89.72"))
        assertFalse(
            OwnAccountTransfer.matches(
                title = "Interbank",
                text = "Transferencia recibida S/ 100.00"
            )
        )
        assertFalse(
            OwnAccountTransfer.matches(
                title = "Banco",
                text = "Transferencia a otra persona S/ 100.00"
            )
        )
    }
}
