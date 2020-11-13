package cloud.pace.sdk.poikit.poi.download

import android.location.Location

object DynamicFilterCache {
    private var cache: List<DynamicFilter> = listOf()
    var userLocation: Location? = null
    var cachedFilterLocation: Location? = null

    fun cacheFilter(filters: List<DynamicFilter>?, location: Location) {
        if (filters != null) {
            cache = filters
            cachedFilterLocation = location
        }
    }

    fun getFilterCache(): List<DynamicFilter> {
        return cache
    }
}
