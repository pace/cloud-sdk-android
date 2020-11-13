package cloud.pace.sdk.poikit.search

import android.location.Location

/**
 * Address search request for Nominatim full address search.
 *
 * See also: https://wiki.openstreetmap.org/wiki/Nominatim#Address_lookup
 */

data class AddressSearchRequest(
    val acceptLanguages: List<String>? = null,
    val text: String,
    val locationBias: Location? = null,
    val limit: Int = 10,
    val includeKeys: List<String>? = null,
    val excludeValues: List<String>? = null
)
