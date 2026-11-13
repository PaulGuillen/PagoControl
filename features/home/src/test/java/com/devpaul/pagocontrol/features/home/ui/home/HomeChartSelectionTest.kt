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
        assertEquals(Calendar.NOVEMBER, currentChartBar(monthly = true, date = date))
    }

    @Test fun weekStartsOnMondayAndEndsOnSunday() {
        assertEquals(0, currentChartBar(monthly = false, date = date(day = 9)))
        assertEquals(6, currentChartBar(monthly = false, date = date(day = 15)))
    }

    @Test fun monthlySelectionUsesCalendarMonthRegardlessOfDay() {
        for (month in Calendar.JANUARY..Calendar.DECEMBER) {
            listOf(1, 14, 28).forEach { day ->
                val calendar = date(day).apply { set(Calendar.MONTH, month) }
                assertEquals(month, currentChartBar(monthly = true, date = calendar))
            }
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
