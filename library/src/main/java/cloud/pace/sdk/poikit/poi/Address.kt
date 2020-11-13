package cloud.pace.sdk.poikit.poi

import android.os.Parcelable
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_ADDRESS
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Represents an address with house number and street name.
 */

@Parcelize
class Address(val encodedAddress: String) : Parcelable {
    @IgnoredOnParcel
    var countryCode: String? = null

    @IgnoredOnParcel
    var city: String? = null

    @IgnoredOnParcel
    var postalCode: String? = null

    @IgnoredOnParcel
    var suburb: String? = null

    @IgnoredOnParcel
    var state: String? = null

    @IgnoredOnParcel
    var street: String? = null

    @IgnoredOnParcel
    var houseNumber: String? = null

    init {
        encodedAddress.split(";").forEach {
            val components = it.split("=")

            if (components.size != 2) {
                return@forEach
            }

            when (components[0]) {
                TAG_COUNTRY_CODE -> countryCode = components[1]
                TAG_CITY -> city = components[1]
                TAG_POSTAL_CODE -> postalCode = components[1]
                TAG_SUBURB -> suburb = components[1]
                TAG_STATE -> state = components[1]
                TAG_STREET -> street = components[1]
                TAG_HOUSE_NUMBER -> houseNumber = components[1]
            }
        }
    }

    override fun toString(): String {
        return "$countryCode; $city; $postalCode; $suburb; $state; $street; $houseNumber"
    }

    companion object {
        const val TAG_COUNTRY_CODE = "c"
        const val TAG_CITY = "l"
        const val TAG_POSTAL_CODE = "pc"
        const val TAG_SUBURB = "su"
        const val TAG_STATE = "st"
        const val TAG_STREET = "s"
        const val TAG_HOUSE_NUMBER = "hn"
    }
}

fun HashMap<String, String>.toAddress(): Address? {
    val encodedAddress = this[OSM_ADDRESS] ?: return null
    return Address(encodedAddress)
}
