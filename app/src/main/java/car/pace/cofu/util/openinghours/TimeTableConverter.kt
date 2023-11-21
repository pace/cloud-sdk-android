package car.pace.cofu.util.openinghours

import cloud.pace.sdk.poikit.poi.OpeningHours

/**
 * Interface for converting a custom timetable format to a TimeTable object.
 */
interface TimeTableConverter {
    /**
     * converts a custom timetable format to a TimeTable object.
     * @param sourceData the source data delivered by a given endpoint
     * @return the time table represented by the source data
     */
    fun convertToTimetable(sourceData: List<OpeningHours>): TimeTable
}
