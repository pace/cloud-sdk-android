/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

/* Creates a new event object at lat/lng from this POI ID */
class MoveRequestBody {

    var attributes: Attributes? = null

    /* UUID of the POI that is going to be moved */
    var id: String? = null
    var type: Type? = null

    /* Creates a new event object at lat/lng from this POI ID */
    enum class Type(val value: String) {
        @SerializedName("movePoi")
        @Json(name = "movePoi")
        MOVEPOI("movePoi")
    }

    /* Creates a new event object at lat/lng from this POI ID */
    class Attributes {

        /* Latitude in degrees */
        var latitude: Float? = null

        /* Longitude in degrees */
        var longitude: Float? = null
    }
}
