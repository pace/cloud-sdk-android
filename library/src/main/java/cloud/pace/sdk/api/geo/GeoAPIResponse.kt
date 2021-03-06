package cloud.pace.sdk.api.geo

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
    val appUrls: List<String>
)

data class CofuGasStation(
    var id: String,
    var coordinate: LatLng,
    var connectedFuelingStatus: ConnectedFuelingStatus
)

enum class ConnectedFuelingStatus(val value: String) {
    ONLINE("online"),
    OFFLINE("offline")
}
