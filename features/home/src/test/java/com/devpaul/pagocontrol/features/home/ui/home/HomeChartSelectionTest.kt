package com.devpaul.pagocontrol.features.home.ui.home

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HomeChartSelectionTest {
    @Test fun thursdayUsesTheFourthBarRatherThanSaturday() {
        val date = date(day = 12)
        assertEquals(Calendar.THURSDAY, date.get(Calendar.DAY_OF_WEEK))
        assertEquals(3, currentChartBar(monthly = false, date = date))
        assertEquals(1, currentChartBar(monthly = true, date = date))
    }

    @Test fun weekStartsOnMondayAndEndsOnSunday() {
        assertEquals(0, currentChartBar(monthly = false, date = date(day = 9)))
        assertEquals(6, currentChartBar(monthly = false, date = date(day = 15)))
    }

    @Test fun monthUsesTheSameSevenDayBucketsAsTheDashboard() {
        listOf(1 to 0, 7 to 0, 8 to 1, 14 to 1, 15 to 2, 21 to 2, 22 to 3, 28 to 3, 29 to 4, 30 to 4).forEach { (day, expected) ->
            assertEquals(expected, currentChartBar(monthly = true, date = date(day)))
        }
    }

    @Test fun dateIdentityChangesAtMidnightAndWithTimeZone() {
        assertNotEquals(currentChartDate(date(12)), currentChartDate(date(13)))
        val otherZone = date(12).apply { timeZone = TimeZone.getTimeZone("UTC") }
        assertNotEquals(currentChartDate(date(12)), currentChartDate(otherZone))
    }

    private fun date(day: Int): Calendar = GregorianCalendar(TimeZone.getTimeZone("America/Lima")).apply {
        clear()
        set(2026, Calendar.NOVEMBER, day, 12, 0)
    }
}
