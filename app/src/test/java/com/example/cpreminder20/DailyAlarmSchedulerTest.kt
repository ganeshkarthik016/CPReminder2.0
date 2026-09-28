package com.example.cpreminder20

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DailyAlarmSchedulerTest {
    private val kolkata = TimeZone.getTimeZone("Asia/Kolkata")

    @Test
    fun schedulesTodayWhenBeforeTenThirtyPm() {
        val now = localTime(2026, Calendar.SEPTEMBER, 28, 18, 0)
        val expected = localTime(2026, Calendar.SEPTEMBER, 28, 22, 30)
        assertEquals(expected, DailyAlarmScheduler.nextOccurrence(now, kolkata))
    }

    @Test
    fun schedulesTomorrowWhenAtOrAfterTenThirtyPm() {
        val atCheck = localTime(2026, Calendar.SEPTEMBER, 28, 22, 30)
        val expected = localTime(2026, Calendar.SEPTEMBER, 29, 22, 30)
        assertEquals(expected, DailyAlarmScheduler.nextOccurrence(atCheck, kolkata))
        assertEquals(expected, DailyAlarmScheduler.nextOccurrence(atCheck + 1, kolkata))
    }

    @Test
    fun preservesTenThirtyLocalTimeAcrossTimeZones() {
        val zones = listOf("Asia/Kolkata", "America/Los_Angeles", "Australia/Sydney")
        for (zoneId in zones) {
            val zone = TimeZone.getTimeZone(zoneId)
            val now = localTime(2026, Calendar.SEPTEMBER, 28, 12, 0, zone)
            val result = Calendar.getInstance(zone).apply {
                timeInMillis = DailyAlarmScheduler.nextOccurrence(now, zone)
            }
            assertEquals(22, result.get(Calendar.HOUR_OF_DAY))
            assertEquals(30, result.get(Calendar.MINUTE))
        }
    }

    @Test
    fun handlesCrossingMidnight() {
        val now = localTime(2026, Calendar.SEPTEMBER, 28, 23, 59)
        val result = Calendar.getInstance(kolkata).apply {
            timeInMillis = DailyAlarmScheduler.nextOccurrence(now, kolkata)
        }
        assertEquals(Calendar.SEPTEMBER, result.get(Calendar.MONTH))
        assertEquals(29, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(22, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, result.get(Calendar.MINUTE))
    }

    private fun localTime(
        year: Int, month: Int, day: Int, hour: Int, minute: Int,
        zone: TimeZone = kolkata
    ): Long = Calendar.getInstance(zone).apply {
        set(year, month, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
