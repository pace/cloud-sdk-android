package cloud.pace.sdk.poikit.poi

import TileQueryRequestOuterClass
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.gasStations
import cloud.pace.sdk.api.poi.generated.request.gasStations.GetGasStationAPI.getGasStation
import cloud.pace.sdk.poikit.database.GasStationDAO
import cloud.pace.sdk.poikit.poi.download.TileDownloader
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.poikit.utils.addPadding
import cloud.pace.sdk.poikit.utils.toTileQueryRequest
import cloud.pace.sdk.utils.*
import com.google.android.gms.maps.model.VisibleRegion
import kotlinx.coroutines.*
import okhttp3.Call
import org.koin.core.component.inject
import java.util.*

open class PoiKitObserverToken : CloudSDKKoinComponent {

    internal val tileDownloader: TileDownloader by inject()
    val loading = MutableLiveData<Boolean>()
    var lastRefreshTime: Date? = null

    open fun refresh(zoomLevel: Int = POIKitConfig.ZOOMLEVEL) {
        lastRefreshTime = Date()
    }

    open fun invalidate() {}
}

class VisibleRegionNotificationToken(
    val visibleRegion: VisibleRegion,
    padding: Double,
    private val gasStationDao: GasStationDAO,
    private val completion: (Completion<List<PointOfInterest>>) -> Unit
) : PoiKitObserverToken() {

    private var gasStationsObserver: Observer<List<GasStation>>? = null
    private var gasStations: LiveData<List<GasStation>>? = null
    private var downloadTask: Call? = null

    init {
        // load all the points that are around a certain radius of the visible center
        val regionToLoad = visibleRegion.addPadding(padding)

        gasStations = gasStationDao.getInBoundingBoxLive(
            minLat = regionToLoad.latLngBounds.southwest.latitude,
            minLon = regionToLoad.latLngBounds.southwest.longitude,
            maxLat = regionToLoad.latLngBounds.northeast.latitude,
            maxLon = regionToLoad.latLngBounds.northeast.longitude
        )

        gasStationsObserver = Observer {
            completion(Success(it))
        }

        gasStationsObserver?.let { gasStations?.observeForever(it) }
    }

    override fun refresh(zoomLevel: Int) {
        if (gasStationsObserver == null) return

        loading.value = true

        val tileRequest = visibleRegion.toTileQueryRequest(zoomLevel)

        downloadTask = tileDownloader.load(tileRequest) {
            it.onSuccess { stations ->
                onIOBackgroundThread {
                    gasStationDao.insertGasStations(stations)

                    withContext(Dispatchers.Main) { loading.value = false }

                    // Delete gas stations not reported by new tiles anymore
                    val persistedGasStations = gasStationDao.getInBoundingBox(
                        minLat = visibleRegion.latLngBounds.southwest.latitude,
                        minLon = visibleRegion.latLngBounds.southwest.longitude,
                        maxLat = visibleRegion.latLngBounds.northeast.latitude,
                        maxLon = visibleRegion.latLngBounds.northeast.longitude
                    )
                    val outdatedStations = persistedGasStations.filter { it.id !in stations.map { it.id } }
                    gasStationDao.delete(outdatedStations)
                }
            }

            it.onFailure { error ->
                completion(Failure(error))
                onMainThread { loading.value = false }
            }
        }

        super.refresh(zoomLevel)
    }

    override fun invalidate() {
        gasStationsObserver?.let { gasStations?.removeObserver(it) }
        downloadTask?.cancel()
    }
}

class IDsNotificationToken(
    private val ids: List<String>,
    private val gasStationDao: GasStationDAO,
    private val completion: (Completion<List<PointOfInterest>>) -> Unit
) : PoiKitObserverToken() {

    private val gasStations = gasStationDao.getByIdsLive(ids)
    private val gasStationsObserver = Observer<List<GasStation>> { completion(Success(it)) }
    private var downloadTask: Call? = null

    init {
        gasStations.observeForever(gasStationsObserver)
    }

    override fun refresh(zoomLevel: Int) {
        loading.value = true

        CoroutineScope(Dispatchers.Default).launch {
            val dbStations = gasStationDao.getByIds(ids)
            // Gas station we don't have in the database
            val missingStationIds = ids - dbStations.map { it.id }
            // Database stations without location
            val stationsWithoutLocations = dbStations.filter { it.center == null }.map { it.id }
            // List of unknown gas station locations
            val stationsToFetch = (missingStationIds + stationsWithoutLocations).toSet()

            if (stationsToFetch.isNotEmpty()) {
                // First fetch gas stations to get locations for tile request
                try {
                    val tileRequest = stationsToFetch
                        .map {
                            async {
                                getGasStation(it)
                            }
                        }
                        .awaitAll()
                        .mapNotNull {
                            val latitude = it?.latitude?.toDouble()
                            val longitude = it?.longitude?.toDouble()
                            if (latitude != null && longitude != null) {
                                LocationPoint(latitude, longitude)
                            } else {
                                null
                            }
                        }
                        .plus(dbStations.mapNotNull { it.center }) // List of gas stations we already have in the database
                        .toTileQueryRequest(zoomLevel)

                    download(tileRequest)
                } catch (e: Exception) {
                    completion(Failure(e))
                }
            } else {
                // We already have all gas station locations, download the data from the tiles right now
                val tileRequest = dbStations.mapNotNull { it.center }.toTileQueryRequest(zoomLevel)
                download(tileRequest)
            }
        }

        super.refresh(zoomLevel)
    }

    override fun invalidate() {
        gasStations.removeObserver(gasStationsObserver)
        downloadTask?.cancel()
    }

    private suspend fun download(tileRequest: TileQueryRequestOuterClass.TileQueryRequest) = withContext(Dispatchers.IO) {
        downloadTask = tileDownloader.load(tileRequest) {
            it.onSuccess { stations ->
                onIOBackgroundThread {
                    gasStationDao.insertGasStations(stations)
                    withContext(Dispatchers.Main) { loading.value = false }
                }
            }

            it.onFailure { error ->
                completion(Failure(error))
                onMainThread { loading.value = false }
            }
        }
    }
}

class IDNotificationToken(
    private val id: String,
    private val gasStationDao: GasStationDAO,
    private val completion: (Completion<GasStation>) -> Unit
) : PoiKitObserverToken() {

    private val gasStation = gasStationDao.getByIdsLive(listOf(id))
    private val gasStationObserver = Observer<List<GasStation>> { it.firstOrNull()?.let { station -> completion(Success(station)) } }
    private var downloadTask: Call? = null

    init {
        gasStation.observeForever(gasStationObserver)
    }

    override fun refresh(zoomLevel: Int) {
        loading.value = true

        onIOBackgroundThread {
            val location = gasStationDao.getByIds(listOf(id)).mapNotNull { it.center }.firstOrNull()
            if (location != null) {
                download(location, zoomLevel)
            } else {
                try {
                    val gasStation = getGasStation(id)
                    val latitude = gasStation?.latitude?.toDouble()
                    val longitude = gasStation?.longitude?.toDouble()
                    if (latitude != null && longitude != null) {
                        download(LocationPoint(latitude, longitude), zoomLevel)
                    } else {
                        completion(Failure(Exception("Latitude, longitude or gas station itself is null. Gas station ID: $id")))
                    }
                } catch (e: Exception) {
                    completion(Failure(e))
                }
            }
        }

        super.refresh(zoomLevel)
    }

    override fun invalidate() {
        gasStation.removeObserver(gasStationObserver)
        downloadTask?.cancel()
    }

    private fun download(location: LocationPoint, zoomLevel: Int) {
        downloadTask = tileDownloader.load(location.toTileQueryRequest(zoomLevel)) {
            it.onSuccess { stations ->
                onIOBackgroundThread {
                    gasStationDao.insertGasStations(stations)
                    withContext(Dispatchers.Main) { loading.value = false }
                }
            }

            it.onFailure { error ->
                completion(Failure(error))
                onMainThread { loading.value = false }
            }
        }
    }
}

class LocationsNotificationToken(
    private val locations: Map<String, LocationPoint>,
    private val gasStationDao: GasStationDAO,
    private val completion: (Completion<List<PointOfInterest>>) -> Unit
) : PoiKitObserverToken() {

    private val gasStations = gasStationDao.getByIdsLive(locations.map { it.key })
    private val gasStationsObserver = Observer<List<GasStation>> { completion(Success(it)) }
    private var downloadTask: Call? = null

    init {
        gasStations.observeForever(gasStationsObserver)
    }

    override fun refresh(zoomLevel: Int) {
        loading.value = true

        val tileRequest = locations.values.toTileQueryRequest(zoomLevel)
        downloadTask = tileDownloader.load(tileRequest) {
            it.onSuccess { stations ->
                onIOBackgroundThread {
                    gasStationDao.insertGasStations(stations)
                    withContext(Dispatchers.Main) { loading.value = false }
                }
            }

            it.onFailure { error ->
                completion(Failure(error))
                onMainThread { loading.value = false }
            }
        }

        super.refresh(zoomLevel)
    }

    override fun invalidate() {
        gasStations.removeObserver(gasStationsObserver)
        downloadTask?.cancel()
    }
}

suspend fun getGasStation(id: String) = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine<cloud.pace.sdk.api.poi.generated.model.GasStation?> { continuation ->
        API.gasStations.getGasStation(id, false).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    continuation.resumeIfActive(body)
                } else {
                    continuation.resumeIfActive(null)
                }
            }

            onFailure = {
                continuation.resumeIfActive(null)
            }
        }
    }
}
