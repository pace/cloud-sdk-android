/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

class UpdateUserPhoneBody {

    var attributes: Attributes? = null
    var id: String? = null
    var type: Type? = null

    enum class Type(val value: String) {
        @SerializedName("phone")
        @Json(name = "phone")
        PHONE("phone")
    }

    class Attributes {

        /* complete phone number of the user */
        lateinit var phoneNumber: String
    }
}