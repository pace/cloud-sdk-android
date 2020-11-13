package cloud.pace.sdk.poikit.search

import android.location.Location
import com.google.gson.annotations.SerializedName

data class PhotonResult(
    @SerializedName("features")
    var features: List<PhotonFeatures>
)

data class PhotonFeatures(
    @SerializedName("properties")
    var properties: PhotonProperty?,

    @SerializedName("type")
    var type: String?,

    @SerializedName("geometry")
    var geometry: PhotonGeometry?
)

data class PhotonProperty(
    @SerializedName("name")
    var name: String?,

    @SerializedName("street")
    var street: String?,

    @SerializedName("housenumber")
    var housenumber: String?,

    @SerializedName("postcode")
    var postcode: String?,

    @SerializedName("state")
    var state: String?,

    @SerializedName("country")
    var country: String?,

    @SerializedName("countrycode")
    var countrycode: String?,

    @SerializedName("osm_key")
    var osm_key: String?,

    @SerializedName("osm_value")
    var osm_value: String?
)

data class PhotonGeometry(
    @SerializedName("type")
    val type: String?,

    @SerializedName("coordinates")
    val coordinates: Array<Double>
) {
    val location: Location
        get() = Location("").also {
            it.latitude = coordinates[1]
            it.longitude = coordinates[0]
        }
}
