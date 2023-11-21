package car.pace.cofu.util.openinghours

/**
 * Model holding a given time range.
 *
 * @param from the start-time of the time range in minutes of day
 * @param to the end-time of the time range in minutes of day
 */
data class TimeRange(var from: Int, var to: Int) {

    override fun toString(): String {
        return "$from-$to"
    }

    fun toHumanReadableString(is24HourFormat: Boolean): String {
        if (to > 1440) to -= 1440
        return formatTime(is24HourFormat, from) + " $DASH " + formatTime(is24HourFormat, to)
    }

    /**
     * Adjusts this time range so that it and the given other range form a new merged time range.
     * The range that this method is called on will get adjusted, while the other one stays the
     * same.
     *
     * @param other the range to merge this range with
     */
    fun mergeWith(other: TimeRange) {
        if (isOverlappingWith(other)) {
            // Use others end time if it ends later than the current one
            if (isEndingEarlierThan(other)) {
                this.to = other.to
            }

            // Use others start time if it starts ealier than the current one
            if (!isStartingEarlierThan(other)) {
                this.from = other.from
            }
        }
    }

    /**
     * Cuts out a range from this range. If the range was completely contained in this range, a
     * new range is generated that represents the latter part of the cutted range, while this
     * range will be the earlier part.
     *
     * @param other the range to cut out
     * @return if the range needs to be split, this will be shortened to the start of the splitting
     * point, and a new range starting after the splitting point will be returned. If not, null is
     * returned
     */
    fun cutOut(other: TimeRange): TimeRange? {
        when {
            contains(other) -> return splitBy(other)
            isCutLeftBy(other) -> this.from = other.to
            isCutRightBy(other) -> this.to = other.from
        }
        return null
    }

    fun contains(time: Int): Boolean {
        return time in from..to
    }

    /**
     * Splits the range by another timeRange
     * @param other the range to split by
     * @return since the range will be split in two, the this object will be the earlier range,
     * while the returned range is the later range.
     */
    private fun splitBy(other: TimeRange): TimeRange {
        val to2 = this.to
        this.to = other.from
        return TimeRange(other.to, to2)
    }

    /**
     * Checks if two ranges overlap
     * @param other another range to check for overlapping
     * @return true, if this range overlaps with the given other range
     */
    fun isOverlappingWith(other: TimeRange): Boolean {
        return other.from in from..to ||
            other.to in from..to ||
            this.from in other.from..other.to ||
            this.to in other.from..other.to
    }

    private fun contains(other: TimeRange): Boolean {
        return other.from in from..to && other.to in from..to
    }

    private fun isCutLeftBy(other: TimeRange): Boolean {
        return other.from !in from..to && other.to in from..to
    }

    private fun isCutRightBy(other: TimeRange): Boolean {
        return other.from in from..to && other.to !in from..to
    }

    private fun isStartingEarlierThan(other: TimeRange): Boolean {
        return from < other.from
    }

    private fun isEndingEarlierThan(other: TimeRange): Boolean {
        return to < other.to
    }

    private fun isValid(): Boolean {
        if (from !in MIN_TIME..MAX_TIME || to !in MIN_TIME..MAX_TIME) return false
        if (from > to) to += 1440
        return true
    }

    fun isWholeDay(): Boolean {
        return from == MIN_TIME && to == MAX_TIME
    }

    companion object {
        private const val MAX_TIME = 24 * 60
        private const val MIN_TIME = 0

        const val DASH: String = "–"

        private fun formatTime(is24HourFormat: Boolean, totalMinutes: Int): String {
            // We cannot use the simple date format class because time zones will mess up.

            var hours = totalMinutes / 60
            val mins = String.format("%02d", totalMinutes % 60)
            return if (is24HourFormat) {
                // Europe and other sane regions.
                "${String.format("%02d", hours)}:$mins"
            } else {
                // Britain.
                when {
                    hours == 24 -> "12:$mins AM"
                    hours == 0 -> "12:$mins AM"
                    hours == 12 -> "12:$mins PM"
                    hours > 12 -> {
                        hours -= 12
                        "${String.format("%02d", hours)}:$mins PM"
                    }

                    else -> // hours between 1 AM and 11 AM
                        "${String.format("%02d", hours)}:$mins AM"
                }
            }
        }
    }
}
