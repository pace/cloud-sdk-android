package car.pace.cofu.util.openinghours

import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule
import cloud.pace.sdk.utils.OpeningHoursUtils.toMinutesOfDay

/**
 * Converter for a POI timetable format to a [TimeTable] instance.
 */
class PoiTimeTableConverter : TimeTableConverter {

    override fun convertToTimetable(sourceData: List<OpeningHours>): TimeTable {
        val data = sourceData
        val timeTable = TimeTable()
        data.forEach { i -> applyRule(timeTable, i) }
        return timeTable
    }

    private fun applyRule(timeTable: TimeTable, rule: OpeningHours) {
        when (rule.rule) {
            OpeningRule.OPEN -> open(timeTable, rule.days, rule.hours)
            OpeningRule.CLOSED -> close(timeTable, rule.days, rule.hours)
            else -> {}
        }
    }

    private fun open(timeTable: TimeTable, days: List<Day>, times: List<OpeningHour>) {
        for (day in days) {
            for (time in times) {
                if (toMinutesOfDay(time.from) == toMinutesOfDay(time.to, true)) {
                    timeTable.addTimeRange(day, 0, 1440)
                } else {
                    timeTable.addTimeRange(day, toMinutesOfDay(time.from), toMinutesOfDay(time.to, true))
                }
            }
        }
    }

    private fun close(timeTable: TimeTable, days: List<Day>, times: List<OpeningHour>) {
        for (day in days) {
            for (time in times) {
                val minutes = time.toMinutesOfDay()
                timeTable.removeTimeRange(day, minutes.first, minutes.second)
            }
        }
    }
}

fun OpeningHour.toMinutesOfDay(): Pair<Int, Int> {
    return Pair(toMinutesOfDay(from, false), toMinutesOfDay(to, true))
}
