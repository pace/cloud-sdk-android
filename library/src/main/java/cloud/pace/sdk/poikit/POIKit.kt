package cloud.pace.sdk.poikit

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.poikit.database.POIKitDatabase
import cloud.pace.sdk.poikit.poi.*
import cloud.pace.sdk.poikit.poi.download.*
import cloud.pace.sdk.poikit.routing.NavigationApiClient
import cloud.pace.sdk.poikit.routing.NavigationMode
import cloud.pace.sdk.poikit.routing.NavigationRequest
import cloud.pace.sdk.poikit.routing.Route
import cloud.pace.sdk.poikit.search.AddressSearchClient
import cloud.pace.sdk.poikit.search.AddressSearchRequest
import cloud.pace.sdk.poikit.search.PhotonResult
import cloud.pace.sdk.utils.*
import com.google.android.gms.maps.model.VisibleRegion
import io.reactivex.rxjava3.core.Observable
import org.koin.core.inject
import java.util.*

object POIKit : POIKitKoinComponent, LifecycleObserver {

    private val database: POIKitDatabase by inject()
    private val navigationApi: NavigationApiClient by inject()
    private val poiApi: PoiApiClient by inject()
    private val addressSearchApi: AddressSearchClient by inject()
    private val dynamicFilterApi: DynamicFilterApiClient by inject()
    private val priceHistoryApi: PriceHistoryApiClient by inject()
    private val gasStationApi: GasStationApiClient by inject()
    private val locationProvider: LocationProvider by inject()
    var maxPoiSearchBoxSize = 15000.0

    fun setup(context: Context, environment: Environment, deviceId: String) {
        KoinConfig.setupPOIKit(context, environment, deviceId)
        TileDownloader.env = environment
    }

    fun startLocationListener(): LocationProvider {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        return locationProvider.also { it.requestLocationUpdates() }
    }

    fun stopLocationListener() {
        locationProvider.removeLocationUpdates()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onLifecycleStart() {
        locationProvider.requestLocationUpdates()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onLifecycleStop() {
        stopLocationListener()
    }

    @JvmOverloads
    fun observe(visibleRegion: VisibleRegion, withMaxPoiSearchBoxSize: Boolean = true, completion: (Completion<List<PointOfInterest>>) -> Unit): VisibleRegionNotificationToken {
        return VisibleRegionNotificationToken(visibleRegion, withMaxPoiSearchBoxSize, database.gasStationDao(), completion)
    }

    fun observe(vararg ids: String, completion: (Completion<List<PointOfInterest>>) -> Unit): IDsNotificationToken {
        return IDsNotificationToken(ids.toList(), database.gasStationDao(), completion)
    }

    fun getRoute(destination: LocationPoint, completion: (Completion<Route?>) -> Unit) {
        locationProvider.getLastKnownLocation {
            if (it != null) {
                val navigationRequest = NavigationRequest(
                    UUID.randomUUID().toString(),
                    listOf(LocationPoint(it.latitude, it.longitude), destination),
                    alternatives = false,
                    navigationMode = NavigationMode.CAR
                )
                navigationApi.getRoute(navigationRequest, completion)
            } else {
                completion(Failure(Exception("Could not get last known location")))
            }
        }
    }

    fun getRegionalPrice(latitude: Double, longitude: Double, completion: (Completion<List<RegionalPriceResponse>?>) -> Unit) {
        poiApi.getRegionalPrices(latitude, longitude, completion)
    }

    fun searchAddress(request: AddressSearchRequest): Observable<PhotonResult> {
        return addressSearchApi.searchAddress(request)
    }

    fun getDynamicFilters(latitude: Double, longitude: Double, completion: (Completion<DynamicFilterResponse?>) -> Unit) {
        dynamicFilterApi.getDynamicFilters(latitude, longitude, completion)
    }

    fun getPriceHistory(id: String, fuelType: FuelType, from: Date, to: Date, completion: (Completion<PriceHistoryApiResponse?>) -> Unit) {
        priceHistoryApi.getPriceHistory(id, fuelType.value, from, to, completion)
    }

    fun getGasStation(id: String, compileOpeningHours: Boolean, forMovedGasStation: Boolean, completion: (Completion<GasStationMovedResponse>) -> Unit) {
        gasStationApi.getGasStation(id, compileOpeningHours, forMovedGasStation, completion)
    }

    fun getGasStationLocal(vararg ids: String, completion: (Completion<List<GasStation>>) -> Unit) {
        onBackgroundThread {
            val gasStations = database.gasStationDao().getByIds(ids.toList())
            completion(Success(gasStations))
        }
    }

    fun getFromBoundingBoxLocal(minLatitude: Double, maxLatitude: Double, minLongitude: Double, maxLongitude: Double, completion: (Completion<List<GasStation>>) -> Unit) {
        onBackgroundThread {
            val gasStations = database.gasStationDao().getInBoundingBox(minLatitude, maxLatitude, minLongitude, maxLongitude)
            completion(Success(gasStations))
        }
    }

    fun insertGasStations(vararg gasStations: GasStation) {
        onBackgroundThread {
            database.gasStationDao().insertGasStations(gasStations.toList())
        }
    }
}
