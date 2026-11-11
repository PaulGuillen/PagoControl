package com.devpaul.features.resume.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeAiDeviceIdTest {
    @Test
    fun `same Android identifier keeps the same quota key across installations`() {
        val firstInstallation = resumeAiDeviceHash("1234567890abcdef")
        val reinstallation = resumeAiDeviceHash("1234567890abcdef")

        assertEquals(firstInstallation, reinstallation)
        assertTrue(firstInstallation.matches(Regex("[a-f0-9]{64}")))
        assertNotEquals("1234567890abcdef", firstInstallation)
    }

    @Test
    fun `different devices have different quota keys`() {
        assertNotEquals(resumeAiDeviceHash("first-device"), resumeAiDeviceHash("second-device"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `missing identifier cannot silently share a quota key`() {
        resumeAiDeviceHash(" ")
    }
}
