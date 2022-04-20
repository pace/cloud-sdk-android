/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.Date

@JsonApi(type = "locationBasedAppWithRefs")
class LocationBasedAppWithRefs : Resource() {

    /* Android instant app URL */
    var androidInstantAppUrl: String? = null
    var appType: AppType? = null

    /* A location-based app is by default loaded on `approaching`. Some apps should be loaded in advance. They have the cache set to `preload`.
 */
    var cache: Cache? = null

    /* Time of LocationBasedApp creation (iso8601 without time zone) */
    var createdAt: Date? = null

    /* Time of LocationBasedApp deletion (iso8601 without time zone) */
    var deletedAt: Date? = null

    /* Logo URL */
    var logoUrl: String? = null

    /* Progressive web application URL. The URL satisfies the following criteria: <li>The URL responds with `text/html` on a GET request</li> <li>The response contains HTTP caching headers e.g. `Cache-Control` and `ETag`</li> <li>HTTP GET request on the URL with an `ETag` will return `304` (`Not Modified`), if the content didn't change</li> <li>If `503` (`Service Unavailable`) is returned the request should be retried later</li> <li>If `404` (`Not Found`) is returned the URL is invalidated and a new app should be requested</li>
 */
    var pwaUrl: String? = null

    /* References are PRNs to external and internal resources that are related to the query */
    var references: List<String>? = null
    var subtitle: String? = null
    var title: String? = null

    /* Time of LocationBasedApp last update (iso8601 without time zone) */
    var updatedAt: Date? = null

    enum class AppType(val value: String) {
        @SerializedName("fueling")
        @Json(name = "fueling")
        FUELING("fueling")
    }

    /* A location-based app is by default loaded on `approaching`. Some apps should be loaded in advance. They have the cache set to `preload`.
     */
    enum class Cache(val value: String) {
        @SerializedName("approaching")
        @Json(name = "approaching")
        APPROACHING("approaching"),

        @SerializedName("preload")
        @Json(name = "preload")
        PRELOAD("preload")
    }
}
