/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

class DedupeRequestBody {

    var attributes: Attributes? = null
    /* UUID of the POI that is considered as origin of all the other POI duplicate UUIDs */
    var id: String? = null
    var type: Type? = null

    enum class Type(val value: String) {
        @SerializedName("dedupePoi")
        @Json(name = "dedupePoi")
        DEDUPEPOI("dedupePoi")
    }

    class Attributes {

        /* UUIDs of the duplicate POIs */
        var duplicates: List<String>? = null
    }
}
