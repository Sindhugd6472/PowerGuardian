package com.example.powerguardian

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageStatsHelperTest {
    @Test fun zeroPaddedMorningHourMatchesTodayOnly() {
        val lines = listOf(
            "2026-10-16 09:15:00 - Screen ON",
            "2026-10-15 09:15:00 - Screen ON",
            "2026-10-16 10:15:00 - Screen OFF"
        )
        assertEquals(33, UsageStatsHelper.eventPercent(lines, "2026-10-16 09:"))
    }
    @Test fun appListContinuationLinesDoNotDiluteEventPercentage() {
        val lines = listOf("Top 5 Apps as of 2026-10-16 09:30:00:", "1. com.example.app - 2 min", "", "2026-10-16 09:40:00 - Screen OFF")
        assertEquals(100, UsageStatsHelper.eventPercent(lines, "2026-10-16 09:"))
    }
    @Test fun missingEventsHaveZeroPercentage() {
        assertEquals(0, UsageStatsHelper.eventPercent(emptyList(), "2026-10-16 00:"))
        assertEquals(0, UsageStatsHelper.eventPercent(listOf("not an event"), "2026-10-16 00:"))
    }
}
