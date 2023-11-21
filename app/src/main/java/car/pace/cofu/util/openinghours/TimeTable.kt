package car.pace.cofu.util.openinghours

import cloud.pace.sdk.poikit.poi.Day

/**
 * Model for a timetable given by a map of days with opening ranges for each day.
 */
class TimeTable {

    val days: List<DayTimes> = Day.values().map { DayTimes(it, arrayListOf()) }

    fun addTimeRange(day: Day, from: Int, to: Int) {
        days[indexOfDay(day)].add(from, to)
    }

    fun removeTimeRange(day: Day, from: Int, to: Int) {
        days[indexOfDay(day)].remove(from, to)
    }

    fun toHumanReadableString(days: Array<String>, closed: String, alwaysOpened: String, daily: String, is24HourFormat: Boolean): MutableList<OpeningHoursItem> {
        val entries: MutableList<OpeningHoursItem> = mutableListOf()

        if (sameOpeningTimesDaily()) {
            entries.add(OpeningHoursItem(daily, this.days[0].toHumanReadableString(closed, alwaysOpened, is24HourFormat)))
        } else {
            var daysString = ""
            var firstSameTimeIndex = 0

            this.days.forEachIndexed { index, day ->
                if (!day.sameOpeningTimesAs(this.days[firstSameTimeIndex])) {
                    daysString = days[firstSameTimeIndex]
                    if (firstSameTimeIndex != index - 1) {
                        daysString += " $DASH "
                        daysString += days[index - 1]
                    }

                    val times = this.days[firstSameTimeIndex].toHumanReadableString(closed, alwaysOpened, is24HourFormat)
                    firstSameTimeIndex = index
                    entries.add(OpeningHoursItem(daysString, times))
                }
            }

            // check sunday
            daysString = days[firstSameTimeIndex]
            if (firstSameTimeIndex != this.days.lastIndex) {
                daysString += " $DASH "
                daysString += days.last()
            }

            val times = this.days[firstSameTimeIndex].toHumanReadableString(closed, alwaysOpened, is24HourFormat)
            entries.add(OpeningHoursItem(daysString, times))
        }

        return entries
    }

    fun sameOpeningTimesDaily(): Boolean {
        return days.distinctBy {
            it.openingTimes
        }.size == 1
    }

    override fun toString(): String {
        return days.joinToString("\n")
    }

    private fun indexOfDay(day: Day): Int {
        return Day.values().indexOf(day)
    }

    companion object {
        const val DASH: String = "–"
    }
}
