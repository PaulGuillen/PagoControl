package com.devpaul.pagocontrol.platform.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseAnalyticsReporterTest {
    @Test fun errorReportsExcludeMessagesAndCausesContainingAccountData() {
        val original = IllegalStateException("private@example.com password123", IllegalArgumentException("token"))
        val sanitized = sanitizedAnalyticsError("login", original)
        assertEquals("login: IllegalStateException", sanitized.message)
        assertEquals(original.stackTrace.toList(), sanitized.stackTrace.toList())
        assertNull(sanitized.cause)
        assertFalse(sanitized.toString().contains("private@example.com"))
    }
}
