/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

class CommonOpeningHours {

    var rules: List<Rules>? = null
    /* As defined by ISO 8601, the timezone */
    var timezone: String? = null

    class Rules {

        var action: Action? = null
        var days: List<Days>? = null
        var timespans: List<Timespans>? = null

        enum class Action(val value: String) {
            @SerializedName("open")
            @Json(name = "open")
            OPEN("open"),
            @SerializedName("close")
            @Json(name = "close")
            CLOSE("close")
        }

        enum class Days(val value: String) {
            @SerializedName("monday")
            @Json(name = "monday")
            MONDAY("monday"),
            @SerializedName("tuesday")
            @Json(name = "tuesday")
            TUESDAY("tuesday"),
            @SerializedName("wednesday")
            @Json(name = "wednesday")
            WEDNESDAY("wednesday"),
            @SerializedName("thursday")
            @Json(name = "thursday")
            THURSDAY("thursday"),
            @SerializedName("friday")
            @Json(name = "friday")
            FRIDAY("friday"),
            @SerializedName("saturday")
            @Json(name = "saturday")
            SATURDAY("saturday"),
            @SerializedName("sunday")
            @Json(name = "sunday")
            SUNDAY("sunday")
        }

        class Timespans {

            /* relative to the specified time zone (local time) */
            var from: String? = null
            /* relative to the specified time zone (local time) */
            var to: String? = null
        }
    }
}
