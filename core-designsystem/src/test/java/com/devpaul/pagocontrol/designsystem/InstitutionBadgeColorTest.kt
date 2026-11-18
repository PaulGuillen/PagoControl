package com.devpaul.pagocontrol.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InstitutionBadgeColorTest {
    @Test
    fun usesBankPackageEvenWhenLabelIsPlin() {
        assertEquals(Color(0xFF009B3A), institutionBadgeColor("Plin", "pe.com.interbank.mobilebanking", false))
        assertEquals(Color(0xFFEC111A), institutionBadgeColor("Plin", "pe.com.scotiabank.blpm.android.client", false))
        assertEquals(Color(0xFF004481), institutionBadgeColor("Plin", "com.bbva.nxt_peru", false))
    }

    @Test
    fun preservesMailAndUnknownPackageFallbacks() {
        assertNull(institutionBadgeColor("Interbank", "com.google.android.gm", true))
        assertEquals(Color(0xFF00B5E2), institutionBadgeColor("Plin", "unknown.app", false))
    }
}
