package cloud.pace.sdk.poikit.routing

import cloud.pace.sdk.poikit.poi.LocationPoint
import java.util.*

/**
 * Navigation routing request.
 */
data class NavigationRequest(
    val uuid: String = UUID.randomUUID().toString(),
    val coordinates: List<LocationPoint>,
    val alternatives: Boolean = true,
    val userCourseInDegrees: Double? = null,
    val steps: Boolean = true,
    val navigationMode: NavigationMode = NavigationMode.CAR,
    val annotations: List<AnnotationType> = listOf(AnnotationType.DISTANCE, AnnotationType.DURATION),
    val geometry: GeometryType = GeometryType.POLYLINE,
    val overview: OverviewType = OverviewType.FULL,
    val bearings: List<Bearing?>? = null // must match the number of coordinates, nil if default values should be used
)

class Bearing(val value: Int, val range: Int) {
    override fun equals(other: Any?): Boolean {
        if (other is Bearing) {
            return other.value == value && other.range == range
        }
        return false
    }

    override fun hashCode(): Int {
        return value + 367 * range
    }

    override fun toString(): String {
        return value.toString() + "," + range.toString()
    }
}

enum class AnnotationType(val apiName: String) {
    NODES("nodes"),
    DISTANCE("distance"),
    DURATION("duration"),
    DATASOURCES("datasources"),
    WEIGHT("weight"),
    SPEED("speed")
}

enum class GeometryType(val apiName: String) {
    POLYLINE("polyline"),
    POLYLINE6("polyline6"),
    GEOJSON("geojson")
}

enum class OverviewType(val apiName: String) {
    SIMPLIFIED("simplified"),
    FULL("full"),
    NONE("false")
}

enum class NavigationMode(val apiName: String) {
    CAR("car"),
    FOOT("foot"),
    BIKE("bike")
}
