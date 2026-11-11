package com.devpaul.features.resume.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeAiQuotaTest {
    @Test
    fun `AI is offered only after the server confirms availability`() {
        val policy = ResumeImportPolicy()
        ResumeAiAvailability.entries.forEach { availability ->
            assertEquals(
                availability == ResumeAiAvailability.Available,
                ResumeAiQuotaStatus(availability).canUseAi(policy),
            )
        }
    }

    @Test
    fun `the feature flag can disable an available allowance`() {
        val status = parseResumeAiQuota(mapOf("status" to "available"))
        assertTrue(status.canUseAi(ResumeImportPolicy()))
        assertFalse(status.canUseAi(ResumeImportPolicy(aiEnhancementEnabled = false)))
    }

    @Test
    fun `a consumed allowance preserves the server deadline and disables AI`() {
        val status = parseResumeAiQuota(mapOf("status" to "limited", "nextAvailableAtMs" to 123456789L))
        assertEquals(ResumeAiAvailability.Limited, status.availability)
        assertEquals(123456789L, status.nextAvailableAtMillis)
        assertFalse(status.canUseAi(ResumeImportPolicy()))
    }

    @Test
    fun `an active reservation disables AI without claiming a weekly use`() {
        val status = parseResumeAiQuota(mapOf("status" to "busy", "nextAvailableAtMs" to 1000L))
        assertEquals(ResumeAiAvailability.Busy, status.availability)
        assertFalse(status.canUseAi(ResumeImportPolicy()))
    }

    @Test
    fun `invalid or incomplete server responses never enable AI`() {
        listOf(
            null,
            "available",
            emptyMap<String, Any>(),
            mapOf("status" to "unexpected"),
            mapOf("status" to "limited"),
            mapOf("status" to "limited", "nextAvailableAtMs" to -1),
            mapOf("status" to "limited", "nextAvailableAtMs" to "123"),
        ).forEach { response ->
            val status = parseResumeAiQuota(response)
            assertEquals(ResumeAiAvailability.Unavailable, status.availability)
            assertFalse(status.canUseAi(ResumeImportPolicy()))
        }
    }
}
