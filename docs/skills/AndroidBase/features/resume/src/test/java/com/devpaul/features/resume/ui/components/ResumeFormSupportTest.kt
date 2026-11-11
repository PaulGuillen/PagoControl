package com.devpaul.features.resume.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeFormSupportTest {

    @Test
    fun `replaceById only updates the selected entry`() {
        val result = listOf("first", "second").replaceById("second", { it }, "updated")

        assertEquals(listOf("first", "updated"), result)
    }

    @Test
    fun `validates optional years`() {
        assertTrue("".isValidYearOrBlank())
        assertTrue("2026".isValidYearOrBlank())
        assertFalse("26".isValidYearOrBlank())
    }

    @Test
    fun `accepts only complete web urls`() {
        assertTrue("https://example.com/profile".isValidWebUrl())
        assertFalse("example.com".isValidWebUrl())
    }

    @Test
    fun `accepts optional month year dates and legacy years`() {
        assertTrue("".isValidMonthYearOrBlank())
        assertTrue("2026".isValidMonthYearOrBlank())
        assertTrue("07/2026".isValidMonthYearOrBlank())
        assertTrue("01/2026".isValidMonthYearOrBlank())
        assertTrue("12/2026".isValidMonthYearOrBlank())
        assertTrue("10/2026".isValidMonthYearOrBlank())
        assertFalse("00/2026".isValidMonthYearOrBlank())
        assertFalse("13/2026".isValidMonthYearOrBlank())
        assertFalse("7/2026".isValidMonthYearOrBlank())
        assertFalse("07/26".isValidMonthYearOrBlank())
        assertFalse("072026".isValidMonthYearOrBlank())
        assertFalse("07-2026".isValidMonthYearOrBlank())
    }
}
