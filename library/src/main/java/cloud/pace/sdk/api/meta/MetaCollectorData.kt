package cloud.pace.sdk.api.meta

data class MetaCollectorData(
    var deviceId: String,
    var clientId: String,
    var locale: String,
    var services: List<MetaCollectorService>,
    var userId: String? = null,
    var lastLocation: MetaCollectorLocation? = null,
    var firebasePushToken: String? = null
)

data class MetaCollectorService(
    val name: String,
    val version: String
)

data class MetaCollectorLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyInM: Int
)
