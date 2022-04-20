/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.user.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.Date

@JsonApi(type = "user")
class User : Resource() {

    var address: Address? = null

    /* End-User's birthday, represented as an ISO 8601:2004 [ISO8601‑2004] `YYYY-MM-DD` format. The year MAY be `0000`, indicating that it is omitted. To represent only the year, `YYYY` format is allowed. Note that depending on the underlying platform's date related function, providing just year can result in varying month and day, so the implementers need to take this factor into account to correctly process the dates.
 */
    var birthDate: Date? = null
    var email: String? = null
    var firstName: String? = null

    /* End-User's gender. Values defined by this specification are `female` and `male`. `other` should be used in all other cases. */
    var gender: Gender? = null
    var lastName: String? = null

    /* End-User's locale, represented as a BCP47 [RFC5646] language tag. This is typically an ISO 639-1 Alpha-2 [ISO639‑1] language code in lowercase and an ISO 3166-1 Alpha-2 [ISO3166‑1] country code in uppercase, separated by a dash. For example, en-US or fr-CA. As a compatibility note, some implementations have used an underscore as the separator rather than a dash, for example, en_US; Relying Parties MAY choose to accept this locale syntax as well.
 */
    var locale: String? = null

    /* End-User's preferred telephone number. E.164 [E.164] is RECOMMENDED as the format of this Claim, for example, +1 (425) 555-1212 or +56 (2) 687 2400. If the phone number contains an extension, it is RECOMMENDED that the extension be represented using the RFC 3966 [RFC3966] extension syntax, for example, +1 (604) 555-1234;ext=5678.
 */
    var phoneNumber: String? = null
    var pictureUrl: String? = null

    /* Timezone as per tz database. */
    var zoneInfo: String? = null

    /* End-User's gender. Values defined by this specification are `female` and `male`. `other` should be used in all other cases. */
    enum class Gender(val value: String) {
        @SerializedName("male")
        @Json(name = "male")
        MALE("male"),

        @SerializedName("female")
        @Json(name = "female")
        FEMALE("female"),

        @SerializedName("other")
        @Json(name = "other")
        OTHER("other")
    }

    class Address {

        var additionalAddressLink: String? = null

        /* ISO 3166 Codes (Alpha 2) */
        var country: String? = null
        var houseNo: String? = null
        var locality: String? = null
        var postalCode: String? = null
        var region: String? = null
        var street: String? = null
    }
}
