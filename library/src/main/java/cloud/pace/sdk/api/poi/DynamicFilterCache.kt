package cloud.pace.sdk.api.poi

import android.location.Location
import cloud.pace.sdk.api.poi.generated.model.Categories

object DynamicFilterCache {
    private var cache: Categories = listOf()
    var userLocation: Location? = null
    var cachedFilterLocation: Location? = null

    fun cacheFilter(filters: Categories?, location: Location) {
        if (filters != null) {
            cache = filters
            cachedFilterLocation = location
        }
    }

    fun getFilterCache(): Categories {
        return cache
    }
}