package com.devpaul.pagocontrol.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidationTest {
    @Test fun rejectsMalformedEmail() {
        listOf("", "carlos", "carlos@", "@pagocontrol.pe", "carlos @mail.pe").forEach {
            assertFalse(LoginValidation.validEmail(it))
        }
        assertTrue(LoginValidation.validEmail(" demo@pagocontrol.pe "))
    }

    @Test fun passwordKeepsItsExactValueAndRequiresEightCharacters() {
        assertFalse(LoginValidation.validPassword("1234567"))
        assertTrue(LoginValidation.validPassword("12345678"))
    }
}
