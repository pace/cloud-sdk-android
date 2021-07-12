package cloud.pace.sdk.poikit.poi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import cloud.pace.sdk.poikit.database.GasStationDAO
import cloud.pace.sdk.poikit.poi.download.TileDownloader
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.poikit.utils.addPadding
import cloud.pace.sdk.poikit.utils.toTileQueryRequest
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import com.google.android.gms.maps.model.VisibleRegion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.Call
import org.koin.core.inject
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
                stations.forEach { station -> station.updatedAt = Date() }
                gasStationDao.insertGasStations(stations)

                MainScope().launch { loading.value = false }

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

            it.onFailure { error ->
                completion(Failure(error))
                MainScope().launch { loading.value = false }
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

        GlobalScope.launch {
            val tileRequest = gasStationDao.getByIds(ids).mapNotNull { it.center }.toTileQueryRequest(zoomLevel)

            downloadTask = tileDownloader.load(tileRequest) {
                it.onSuccess { stations ->
                    stations.forEach { station -> station.updatedAt = Date() }
                    gasStationDao.insertGasStations(stations)
                    MainScope().launch { loading.value = false }
                }

                it.onFailure { error ->
                    completion(Failure(error))
                    MainScope().launch { loading.value = false }
                }
            }
        }

        super.refresh(zoomLevel)
    }

    override fun invalidate() {
        gasStations.removeObserver(gasStationsObserver)
        downloadTask?.cancel()
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

        GlobalScope.launch {
            val tileRequest = locations.values.toTileQueryRequest(zoomLevel)

            downloadTask = tileDownloader.load(tileRequest) {
                it.onSuccess { stations ->
                    stations.forEach { station -> station.updatedAt = Date() }
                    gasStationDao.insertGasStations(stations)
                    MainScope().launch { loading.value = false }
                }

                it.onFailure { error ->
                    completion(Failure(error))
                    MainScope().launch { loading.value = false }
                }
            }
        }

        super.refresh(zoomLevel)
    }

    override fun invalidate() {
        gasStations.removeObserver(gasStationsObserver)
        downloadTask?.cancel()
    }
}
