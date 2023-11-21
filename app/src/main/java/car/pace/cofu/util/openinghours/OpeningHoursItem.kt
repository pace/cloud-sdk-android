package car.pace.cofu.util.openinghours

/**
 * List item for opening hours.
 */
data class OpeningHoursItem(val days: String, val times: String, val closesSoon: Boolean = false)
