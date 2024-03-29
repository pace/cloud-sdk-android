/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

/* Creates a new event object at lat/lng from this POI ID */
@JsonApi(type = "moveRequest")
class MoveRequest : Resource() {

    /* Latitude in degrees */
    var latitude: Float? = null

    /* Longitude in degrees */
    var longitude: Float? = null
}
