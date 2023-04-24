package cloud.pace.sdk.utils

import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule
import java.util.Calendar
import java.util.Date

object OpeningHoursUtils {
    fun isOpen(now: Date, openingHours: List<OpeningHours>): Boolean {
        val cal = Calendar.getInstance().apply {
            time = now
        }
        val weekday = cal.get(Calendar.DAY_OF_WEEK)
        val minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        // Check whether current day contains matching opening hours
        val openingHoursForDay = openingHours.filter { it.days.contains(weekday.toWeekday()) }
        return if (isInOpeningHourRule(openingHoursForDay, minuteOfDay)) {
            true
        } else {
            // Check whether previous day contains opening hours after midnight
            val previousDay = if (weekday > 1) weekday - 1 else 7
            val openingHoursForPreviousDay = openingHours.filter { it.days.contains(previousDay.toWeekday()) }
            return isInOpeningRuleAfterMidnight(openingHoursForPreviousDay, minuteOfDay)
        }
    }

    private fun isInOpeningHourRule(openingHours: List<OpeningHours>, minuteOfDay: Int): Boolean {
        val openingRule = openingHours
            .filter { it.rule == OpeningRule.OPEN }
            .firstOrNull { it.hours.map { toMinutesOfDayRange(it) }.any { minuteOfDay in it } }
        return openingRule != null
    }

    private fun isInOpeningRuleAfterMidnight(openingHours: List<OpeningHours>, minuteOfDay: Int): Boolean {
        val openingRule = openingHours
            .filter { it.rule == OpeningRule.OPEN }
            .filter { it.hours.any { goBeyondMidnight(it) } }
            .firstOrNull { it.hours.map { IntRange(0, toMinutesOfDay(it.to)) }.any { minuteOfDay in it } }
        return openingRule != null
    }

    fun toMinutesOfDay(time: String, isTo: Boolean = false): Int {
        val parts = time.split(":")
        return when {
            parts.isEmpty() -> 0
            else -> {
                var minutes = parts[0].toInt() * 60
                if (isTo && minutes == 0) {
                    minutes = 24 * 60 // convert 0 to 24
                }
                if (parts.size == 1) {
                    minutes // HH
                } else {
                    val minutes2 = parts[1].toInt()
                    if (minutes2 == 59) {
                        minutes + 60 // convert 23:59 to 24:00
                    } else {
                        minutes + parts[1].toInt() // HH:MM
                    }
                }
            }
        }
    }

    private fun toMinutesOfDayRange(openingHour: OpeningHour): IntRange {
        return if (goBeyondMidnight(openingHour)) {
            // Drop the opening hours range that belongs to the next day
            IntRange(toMinutesOfDay(openingHour.from, false), 24 * 60)
        } else {
            (IntRange(toMinutesOfDay(openingHour.from, false), toMinutesOfDay(openingHour.to, true)))
        }
    }

    private fun goBeyondMidnight(openingHour: OpeningHour): Boolean {
        val fromTime = toMinutesOfDay(openingHour.from, false)
        val toTime = toMinutesOfDay(openingHour.to, true)
        return fromTime > toTime
    }

    private fun Int.toWeekday(): Day {
        return when (this) {
            1 -> Day.SUNDAY
            2 -> Day.MONDAY
            3 -> Day.TUESDAY
            4 -> Day.WEDNESDAY
            5 -> Day.THURSDAY
            6 -> Day.FRIDAY
            7 -> Day.SATURDAY
            else -> throw RuntimeException()
        }
    }
}
