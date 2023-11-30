package car.pace.cofu.data.cache

import android.util.LruCache
import cloud.pace.sdk.poikit.poi.GasStation

class GasStationCache(maxSize: Int = 500) : LruCache<String, GasStation>(maxSize) {

    fun put(gasStations: List<GasStation>) {
        gasStations.forEach {
            put(it)
        }
    }

    fun put(gasStation: GasStation) {
        put(gasStation.id, gasStation)
    }

    fun get(ids: Collection<String>): List<GasStation> {
        return ids.mapNotNull { getOrNull(it) }
    }

    /**
     * Wrapper around [LruCache.get], so that the Kotlin compiler understands that this Java function can also return null :/
     */
    fun getOrNull(id: String): GasStation? {
        return get(id)
    }
}
