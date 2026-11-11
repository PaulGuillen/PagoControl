package com.devpaul.pagocontrol.features.reports.ui.reports

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialReportTest {
    @Test fun novemberMatchesReferenceAndAllBreakdownsBalance() {
        val report = demoReport()
        assertEquals(685000L, report.income)
        assertEquals(226950L, report.expense)
        assertEquals(458050L, report.net)
        assertEquals(18, report.incomeCount)
        assertEquals(34, report.expenseCount)
        assertEquals(listOf(78000L, 45000L, 41000L, 28550L, 21000L, 13400L), report.categories.map { it.cents })
        assertEquals(215000L, report.points[2].income)
        assertEquals(62000L, report.points[2].expense)
        assertEquals(18.206, report.change!!, 0.01)
    }

    @Test fun everyRangeConservesCentsAndOperations() {
        for (first in 1..11) for (last in first..11) {
            val report = demoReport(first, last)
            assertEquals(report.expense, report.categories.sumOf { it.cents })
            assertEquals(report.income, report.points.sumOf { it.income })
            assertEquals(report.expense, report.points.sumOf { it.expense })
            assertEquals(report.income + report.expense, report.channels.sumOf { it.cents })
            assertEquals(report.incomeCount + report.expenseCount, report.channels.sumOf { it.operations })
            assertTrue(report.savingsRate in 0.0..100.0)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvertedCustomRange() { demoReport(11, 9) }
}
