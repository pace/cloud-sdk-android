/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.util.Date

class ReferenceStatusBody {

    var attributes: Attributes? = null

    /* Service Provider PRN */
    var id: String? = null

    /* Type */
    var type: Type? = null

    /* Type */
    enum class Type(val value: String) {
        @SerializedName("referenceStatus")
        @Json(name = "referenceStatus")
        REFERENCESTATUS("referenceStatus")
    }

    class Attributes {

        /* Availability status of the referenced resource */
        var status: Status? = null

        /* Time of status last update (iso8601) */
        var updatedAt: Date? = null

        /* Availability status of the referenced resource */
        enum class Status(val value: String) {
            @SerializedName("online")
            @Json(name = "online")
            ONLINE("online"),

            @SerializedName("offline")
            @Json(name = "offline")
            OFFLINE("offline")
        }
    }
}
