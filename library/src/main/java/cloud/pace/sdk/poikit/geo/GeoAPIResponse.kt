package cloud.pace.sdk.poikit.geo

import cloud.pace.sdk.poikit.utils.distanceTo
import com.google.android.gms.maps.model.LatLng

const val POLYGON_NAME = "Polygon"
const val POINT_NAME = "Point"
const val GEOMETRY_COLLECTION_NAME = "GeometryCollection"

data class GeoAPIResponse(
    val type: String,
    val features: List<GeoAPIFeature> = emptyList()
)

data class GeoAPIFeature(
    val id: String,
    val type: String,
    val geometry: Geometry,
    val properties: Map<String, Any> = emptyMap()
)

sealed class Geometry(val type: String)
data class Polygon(val coordinates: List<List<List<Double>>>) : Geometry(POLYGON_NAME)
data class Point(val coordinates: List<Double>) : Geometry(POINT_NAME)
data class GeometryCollection(val geometries: List<Geometry>) : Geometry(GEOMETRY_COLLECTION_NAME)

data class GeoGasStation(
    val id: String,
    val appUrls: Map<String, Collection<String>>
)

data class CofuGasStation @JvmOverloads constructor(
    val id: String,
    val coordinate: LatLng,
    val connectedFuelingStatus: ConnectedFuelingStatus? = null,
    val properties: Map<String, Any> = emptyMap()
)

enum class ConnectedFuelingStatus(val value: String) {
    ONLINE("online"),
    OFFLINE("offline")
}

fun GeoAPIFeature.coordinates(): List<LatLng> {
    return when (geometry) {
        is GeometryCollection -> {
            // Check if points are available
            geometry.geometries.filterIsInstance<Point>().flatMap { point ->
                point.toLatLngs()
            }.ifEmpty {
                // Use polygons as fallback (v1)
                geometry.geometries.filterIsInstance<Polygon>().flatMap { polygon ->
                    polygon.toLatLngs()
                }
            }
        }
        is Point -> {
            // Check if points are available
            geometry.toLatLngs()
        }
        is Polygon -> {
            // Use polygons as fallback (v1)
            geometry.toLatLngs()
        }
    }
}

fun GeoAPIFeature.isInRange(latitude: Double, longitude: Double, distanceThresholdInMeters: Int): Boolean {
    return coordinates().any { coordinate ->
        // Filter based on distance to point or polygon
        coordinate.distanceTo(LatLng(latitude, longitude)) < distanceThresholdInMeters
    }
}

fun Point.toLatLngs(): List<LatLng> {
    val lat = coordinates.lastOrNull()
    val lng = coordinates.firstOrNull()

    return if (lat != null && lng != null) {
        listOf(LatLng(lat, lng))
    } else {
        emptyList()
    }
}

fun Polygon.toLatLngs(): List<LatLng> {
    return coordinates.flatMap { ring ->
        ring.mapNotNull { coordinate ->
            val lat = coordinate.lastOrNull()
            val lng = coordinate.firstOrNull()
            if (lat != null && lng != null) {
                LatLng(lat, lng)
            } else {
                null
            }
        }
    }
}
