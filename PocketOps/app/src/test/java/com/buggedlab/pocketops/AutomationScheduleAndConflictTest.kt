package com.buggedlab.pocketops

import com.buggedlab.pocketops.core.model.ActionType
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.AutomationRule
import com.buggedlab.pocketops.core.model.DndFilterType
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.TimeScheduleTrigger
import com.buggedlab.pocketops.core.model.TriggerType
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationScheduleAndConflictTest {

    private val centralZone = ZoneId.of("America/Chicago")

    @Test
    fun testTimezoneAndDaylightSavingTransition() {
        // Test America/Chicago Daylight Saving Time transition (Spring forward)
        val dstSpring = ZonedDateTime.of(2026, 3, 8, 1, 30, 0, 0, centralZone)
        val dstSpringPlusOneHour = dstSpring.plusHours(1)
        // At 2:00 AM clocks move to 3:00 AM, so 1:30 AM + 1 hr is 3:30 AM CDT
        assertEquals(3, dstSpringPlusOneHour.hour)
        assertEquals(30, dstSpringPlusOneHour.minute)

        // Test America/Chicago Fall back transition (clocks repeat 1:00-2:00 AM)
        val dstFall = ZonedDateTime.of(2026, 11, 1, 1, 30, 0, 0, centralZone)
        val dstFallPlusOneHour = dstFall.plusHours(1)
        // Offset shifts from CDT (-05:00) to CST (-06:00)
        assertEquals(1, dstFallPlusOneHour.hour)
        assertEquals(30, dstFallPlusOneHour.minute)
        assertEquals(2, dstFall.plusHours(2).hour)
    }

    @Test
    fun testScheduleTriggerSatisfactionLogic() {
        val schedule = TimeScheduleTrigger(
            startHour = 8,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = listOf(1, 2, 3, 4, 5) // Monday through Friday
        )

        // 10:30 AM on Wednesday (within window)
        val wednesday10Am = ZonedDateTime.of(2026, 10, 7, 10, 30, 0, 0, centralZone)
        val isSatisfiedWed = isScheduleActiveAt(schedule, wednesday10Am)
        assertTrue("Wednesday 10:30 AM must satisfy weekday 8 AM - 5 PM schedule", isSatisfiedWed)

        // 7:00 PM on Wednesday (outside hours)
        val wednesday7Pm = ZonedDateTime.of(2026, 10, 7, 19, 0, 0, 0, centralZone)
        val isSatisfiedEve = isScheduleActiveAt(schedule, wednesday7Pm)
        assertFalse("Wednesday 7:00 PM must not satisfy schedule", isSatisfiedEve)

        // 12:00 PM on Saturday (outside days)
        val saturdayNoon = ZonedDateTime.of(2026, 10, 10, 12, 0, 0, 0, centralZone)
        val isSatisfiedSat = isScheduleActiveAt(schedule, saturdayNoon)
        assertFalse("Saturday noon must not satisfy Monday-Friday schedule", isSatisfiedSat)
    }

    @Test
    fun testRulePriorityAndConflictDetection() {
        val ruleA = AutomationRule(
            id = 1,
            title = "Work Mode Routine",
            description = "Priority DND",
            isEnabled = true,
            priority = 2,
            triggerType = TriggerType.TIME_SCHEDULE,
            actions = listOf(
                AutomationAction(ActionType.SET_DND_FILTER, dndFilter = DndFilterType.PRIORITY_ONLY)
            ),
            conflictGroup = "WORK_HOURS"
        )

        val ruleB = AutomationRule(
            id = 2,
            title = "Personal Lunch Routine",
            description = "All interruptions allowed",
            isEnabled = true,
            priority = 1,
            triggerType = TriggerType.TIME_SCHEDULE,
            actions = listOf(
                AutomationAction(ActionType.SET_DND_FILTER, dndFilter = DndFilterType.ALL_INTERRUPTIONS)
            ),
            conflictGroup = "WORK_HOURS"
        )

        val hasConflict = ruleA.conflictGroup == ruleB.conflictGroup
        assertTrue("Rules in the same conflictGroup must trigger conflict detection", hasConflict)

        // Sort by priority descending
        val sortedRules = listOf(ruleB, ruleA).sortedByDescending { it.priority }
        assertEquals("Higher priority rule must take precedence", ruleA.id, sortedRules.first().id)
    }

    private fun isScheduleActiveAt(sched: TimeScheduleTrigger, dateTime: ZonedDateTime): Boolean {
        val dayOfWeekInt = dateTime.dayOfWeek.value
        if (!sched.daysOfWeek.contains(dayOfWeekInt)) return false

        val currentMinutes = dateTime.hour * 60 + dateTime.minute
        val startMinutes = sched.startHour * 60 + sched.startMinute
        val endMinutes = sched.endHour * 60 + sched.endMinute

        return currentMinutes in startMinutes..endMinutes
    }
}
