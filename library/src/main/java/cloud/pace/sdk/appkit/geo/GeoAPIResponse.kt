package cloud.pace.sdk.appkit.geo

data class GeoAPIResponse(
    val type: String?,
    val features: List<GeoAPIFeature>?
)

data class GeoAPIFeature(
    val id: String?,
    val type: String?,
    val geometry: GeoAPIGeometry?,
    val properties: GeoAPIProperties?
)

data class GeoAPIGeometry(
    val type: String?,
    val coordinates: List<GeoAPICoordinates>?
)

data class GeoAPIProperties(
    val apps: List<GeoAPIApp>?
)

data class GeoAPIApp(
    val type: String?,
    val url: String?
)

typealias GeoAPICoordinates = List<GeoAPICoordinate>
typealias GeoAPICoordinate = List<Double>

data class GeoGasStation(
    val id: String,
    val apps: List<GeoAPIApp>
)
