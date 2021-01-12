package cloud.pace.sdk.api.poi

class GasStationCodes {
    companion object {
        const val STATUS_OK = 200
        const val STATUS_MOVED = 301
        const val STATUS_NOT_FOUND = 404

        const val HEADER_LOCATION = "location"
    }
}

data class GasStationMovedResponse(
    var id: String?,
    var hasChanged: Boolean,
    var latitude: Double?,
    var longitude: Double?
)
