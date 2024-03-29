/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.Date

@JsonApi(type = "source")
class Source : Resource() {

    /* list of ISO-3166-1 ALPHA-2 encoded countries */
    var countries: List<String>? = null
    var createdAt: Date? = null

    /* timestamp of last import from source */
    var lastDataAt: Date? = null

    /* source name, unique */
    var name: String? = null
    var poiType: POIType? = null

    /* JSON field describing the structure of the updates sent by the data source */
    var schema: List<FieldName>? = null
    var updatedAt: Date? = null
}
