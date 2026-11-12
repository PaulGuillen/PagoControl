package com.devpaul.pagocontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentAmountTest {
    @Test fun parsesSolesWithoutFloatingPointRounding() {
        assertEquals(2500L, PaymentAmount.suggest("María te envió S/ 25.00"))
        assertEquals(123456L, PaymentAmount.parse("1,234.56"))
        assertEquals(123456L, PaymentAmount.parse("1.234,56"))
        assertEquals(990L, PaymentAmount.parse("9,9"))
    }

    @Test fun requiresReviewForAmbiguousOrUnsupportedAmounts() {
        assertNull(PaymentAmount.suggest("Pagaste S/ 25.00. Saldo S/ 100.00"))
        assertNull(PaymentAmount.suggest("Compra USD 25.00"))
        assertNull(PaymentAmount.parse("1,234"))
        assertNull(PaymentAmount.parse("0"))
        assertNull(PaymentAmount.parse("-20.00"))
        assertNull(PaymentAmount.parse("9999999999999999999999"))
    }
}
