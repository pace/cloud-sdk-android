package car.pace.cofu.util.openinghours

import cloud.pace.sdk.poikit.poi.Day

/**
 * Model holding values for a given day. The opening times are modelled as an array of timeranges for
 * a given day, ordered from 00:00 to 23:49.
 *
 * @param day the day the given time ranges apply to
 * @param openingTimes the times (they will be shrunk/merged automatically)
 */
class DayTimes(val day: Day, val openingTimes: ArrayList<TimeRange>) {

    override fun toString(): String {
        return "${Day.entries.indexOf(day)}: (${openingTimes.joinToString(", ")})"
    }

    fun toHumanReadableString(closed: String, alwaysOpened: String, is24HourFormat: Boolean): String {
        var result = ""
        var sep = ""

        if (openingTimes.isEmpty()) {
            return closed
        } else if (openingTimes.size == 1 && openingTimes[0].isWholeDay()) {
            return alwaysOpened
        }

        openingTimes.forEach {
            result += sep + it.toHumanReadableString(is24HourFormat)
            sep = "\n"
        }
        return result
    }

    /**
     * Adds a new Time Range to the day
     *
     * @param from the start time (in minutes since 00:00)
     * @param to the end time (in minutes since 00:00)
     */
    fun add(from: Int, to: Int) {
        add(TimeRange(from, to))
    }

    /**
     * Removes a time range / splits the time ranges by a given range depending on the overlapping
     * type (containing, left cut, right cut).
     *
     * @param from the start time (in minutes since 00:00)
     * @param to the end time (in minutes since 00:00)
     */
    fun remove(from: Int, to: Int) {
        val addAfterIterating = hashMapOf<TimeRange, Int>()
        val range = TimeRange(from, to)
        for (openingTime in openingTimes) {
            openingTime.cutOut(range)?.let {
                addAfterIterating[it] = openingTimes.indexOf(openingTime)
            }
        }
        var offset = 1
        for (openingTime in addAfterIterating) {
            openingTimes.add(openingTime.value + offset, openingTime.key)
            offset++
        }
        mergeRanges()
    }

    private fun mergeRanges() {
        val templist = ArrayList(openingTimes)
        for (range1 in templist) {
            for (range2 in templist) {
                if (range1 != range2 && range1.isOverlappingWith(range2)) {
                    range1.mergeWith(range2)
                    range2.mergeWith(range1)
                }
            }
        }
        openingTimes.clear()
        openingTimes.addAll(templist.asSequence().filter { it.to != it.from }.distinct())
    }

    private fun add(range: TimeRange) {
        val overlappingRange = getOverlappingTimeRange(range)
        if (overlappingRange != null) {
            overlappingRange.mergeWith(range)
        } else {
            addAtOrderedIndex(range)
        }
        mergeRanges()
    }

    private fun addAtOrderedIndex(range: TimeRange) {
        val insertIndex = openingTimes.withIndex().find { it.value.from > range.from }?.index
            ?: openingTimes.size
        openingTimes.add(insertIndex, range)
    }

    private fun getOverlappingTimeRange(range: TimeRange): TimeRange? {
        return openingTimes.find { it.isOverlappingWith(range) }
    }

    fun sameOpeningTimesAs(other: DayTimes): Boolean {
        return openingTimes == other.openingTimes
    }
}
