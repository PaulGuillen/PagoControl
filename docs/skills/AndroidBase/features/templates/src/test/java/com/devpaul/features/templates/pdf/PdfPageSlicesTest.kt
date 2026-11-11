package com.devpaul.features.templates.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfPageSlicesTest {

    @Test
    fun `uses a quiet row before the hard page boundary`() {
        val slices = calculatePageSlices(
            contentHeight = 180,
            maximumPageHeight = 100,
            rowInkScore = { row -> if (row == 82) 0 else 10 },
        )

        assertEquals(PdfPageSlice(0, 82), slices.first())
        assertEquals(PdfPageSlice(82, 180), slices.last())
    }

    @Test
    fun `covers all content without gaps`() {
        val slices = calculatePageSlices(
            contentHeight = 350,
            maximumPageHeight = 100,
            rowInkScore = { 1 },
        )

        assertEquals(0, slices.first().top)
        assertEquals(350, slices.last().bottom)
        assertTrue(slices.zipWithNext().all { (current, next) -> current.bottom == next.top })
        assertTrue(slices.all { it.height in 1..100 })
    }
}
