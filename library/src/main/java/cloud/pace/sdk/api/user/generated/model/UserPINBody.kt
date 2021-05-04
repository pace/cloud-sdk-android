/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.HasMany
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.*

class UserPINBody {

    var attributes: Attributes? = null
    var id: String? = null
    var type: Type? = null

    enum class Type(val value: String) {
        @SerializedName("pin")
        @Json(name = "pin")
        PIN("pin")
    }

    class Attributes {

        /* 4-digit code */
        lateinit var pin: String
        /* user otp */
        lateinit var otp: String
    }
}
