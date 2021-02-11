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

@JsonApi(type = "policy")
class Policy : Resource() {

    var countryId: CommonCountryId? = null
    /* Time of POI creation in (iso8601 without zone - expects UTC) */
    var createdAt: Date? = null
    var poiType: POIType? = null
    var rules: List<PolicyRule>? = null
    /* Tracks who did last change */
    var userId: String? = null

}