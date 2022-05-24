package cloud.pace.sdk.poikit

import android.location.Location
import androidx.lifecycle.*
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.gasStations
import cloud.pace.sdk.api.poi.POIAPI.metadataFilters
import cloud.pace.sdk.api.poi.POIAPI.prices
import cloud.pace.sdk.api.poi.generated.model.Categories
import cloud.pace.sdk.api.poi.generated.model.RegionalPrices
import cloud.pace.sdk.api.poi.generated.request.gasStations.GetGasStationAPI.getGasStation
import cloud.pace.sdk.api.poi.generated.request.metadataFilters.GetMetadataFiltersAPI.getMetadataFilters
import cloud.pace.sdk.api.poi.generated.request.prices.GetRegionalPricesAPI.getRegionalPrices
import cloud.pace.sdk.poikit.database.POIKitDatabase
import cloud.pace.sdk.poikit.geo.CofuGasStation
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.poi.*
import cloud.pace.sdk.poikit.poi.download.TileDownloader
import cloud.pace.sdk.poikit.pricehistory.PriceHistory
import cloud.pace.sdk.poikit.pricehistory.PriceHistoryClient
import cloud.pace.sdk.poikit.pricehistory.PriceHistoryFuelType
import cloud.pace.sdk.poikit.routing.NavigationApiClient
import cloud.pace.sdk.poikit.routing.NavigationMode
import cloud.pace.sdk.poikit.routing.NavigationRequest
import cloud.pace.sdk.poikit.routing.Route
import cloud.pace.sdk.poikit.search.AddressSearchClient
import cloud.pace.sdk.poikit.search.AddressSearchRequest
import cloud.pace.sdk.poikit.search.PhotonResult
import cloud.pace.sdk.poikit.utils.GasStationCodes
import cloud.pace.sdk.poikit.utils.GasStationMovedResponse
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.utils.*
import com.google.android.gms.maps.model.VisibleRegion
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.util.*

object POIKit : CloudSDKKoinComponent, DefaultLifecycleObserver {

    private val database: POIKitDatabase by inject()
    private val navigationApi: NavigationApiClient by inject()
    private val addressSearchApi: AddressSearchClient by inject()
    private val priceHistoryApi: PriceHistoryClient by inject()
    private val locationProvider: LocationProvider by inject()
    private val tileDownloader: TileDownloader by inject()
    private val geoApiManager: GeoAPIManager by inject()

    /**
     * Checks whether [PACECloudSDK] has been set up correctly before [POIKit] is used, otherwise log SDK warnings.
     */
    init {
        SetupLogger.logSDKWarningIfNeeded()
    }

    fun startLocationListener(): LocationProvider {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        return locationProvider.also { it.requestLocationUpdates() }
    }

    fun stopLocationListener() {
        locationProvider.removeLocationUpdates()
    }

    override fun onStart(owner: LifecycleOwner) {
        locationProvider.requestLocationUpdates()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopLocationListener()
    }

    @JvmOverloads
    fun observe(visibleRegion: VisibleRegion, padding: Double = 0.0, completion: (Completion<List<PointOfInterest>>) -> Unit): VisibleRegionNotificationToken {
        return VisibleRegionNotificationToken(visibleRegion, padding, database.gasStationDao(), completion)
    }

    fun observe(ids: List<String>, completion: (Completion<List<PointOfInterest>>) -> Unit): IDsNotificationToken {
        return IDsNotificationToken(ids, database.gasStationDao(), completion)
    }

    fun observe(id: String, completion: (Completion<GasStation>) -> Unit): IDNotificationToken {
        return IDNotificationToken(id, database.gasStationDao(), completion)
    }

    fun observe(locations: Map<String, LocationPoint>, completion: (Completion<List<PointOfInterest>>) -> Unit): LocationsNotificationToken {
        return LocationsNotificationToken(locations, database.gasStationDao(), completion)
    }

    @JvmOverloads
    fun requestGasStations(locations: Map<String, LocationPoint>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL, completion: (Completion<List<GasStation>>) -> Unit) {
        val tileRequest = locations.values.toTileQueryRequest(zoomLevel)

        tileDownloader.load(tileRequest) {
            it.onSuccess { stations ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.gasStationDao().insertGasStations(stations)
                    withContext(Dispatchers.Main) {
                        completion(Success(stations))
                    }
                }
            }

            it.onFailure { error ->
                onMainThread {
                    completion(Failure(error))
                }
            }
        }
    }

    fun getRoute(destination: LocationPoint, completion: (Completion<Route?>) -> Unit) {
        onBackgroundThread {
            when (val location = locationProvider.currentLocation(false)) {
                is Success -> {
                    location.result?.let {
                        val navigationRequest = NavigationRequest(
                            UUID.randomUUID().toString(),
                            listOf(LocationPoint(it.latitude, it.longitude), destination),
                            alternatives = false,
                            navigationMode = NavigationMode.CAR
                        )
                        navigationApi.getRoute(navigationRequest, completion)
                    } ?: completion(Failure(Exception("Could not get current location")))
                }
                is Failure -> completion(Failure(location.throwable))
            }
        }
    }

    fun getRegionalPrice(latitude: Double, longitude: Double, completion: (Completion<List<RegionalPrices>>) -> Unit) {
        API.prices.getRegionalPrices(latitude.toFloat(), longitude.toFloat()).handleCallback(completion)
    }

    fun searchAddress(request: AddressSearchRequest): Observable<PhotonResult> {
        return addressSearchApi.searchAddress(request)
    }

    fun getDynamicFilters(latitude: Double, longitude: Double, completion: (Completion<Categories>) -> Unit) {
        API.metadataFilters.getMetadataFilters(latitude.toFloat(), longitude.toFloat()).handleCallback(completion)
    }

    /**
     * Returns the price history for the specified [country][countryCode].
     *
     * @param countryCode Country code in ISO 3166-1 alpha-2 format.
     * @param since Must be less than now and not more than 1 year ago.
     * @param granularity Number&Unit (m,d,w,M,y); Example: 15m.
     * @param forecast Determines if the response includes a price forecast.
     * @param completion Returns a list of [PriceHistory] objects on success or a [Throwable] on failure.
     */
    @JvmOverloads
    fun getPriceHistoryByCountry(countryCode: String, since: Date, granularity: String, forecast: Boolean = false, completion: (Completion<List<PriceHistory>>) -> Unit) {
        priceHistoryApi.getPricesByCountry(countryCode, since, granularity, forecast, completion)
    }

    /**
     * Returns the price history for the specified [country][countryCode] and [fuel type][fuelType].
     *
     * @param countryCode Country code in ISO 3166-1 alpha-2 format.
     * @param fuelType Fuel type for cars, based on the EU fuel marking.
     * @param since Must be less than now and not more than 1 year ago.
     * @param granularity Number&Unit (m,d,w,M,y); Example: 15m.
     * @param forecast Determines if the response includes a price forecast.
     * @param completion Returns a list of [PriceHistoryFuelType] objects on success or a [Throwable] on failure.
     */
    @JvmOverloads
    fun getPriceHistoryByCountry(countryCode: String, fuelType: String, since: Date, granularity: String, forecast: Boolean = false, completion: (Completion<List<PriceHistoryFuelType>>) -> Unit) {
        priceHistoryApi.getPricesByCountry(countryCode, fuelType, since, granularity, forecast, completion)
    }

    /**
     * Returns the price history for the specified [gas station][stationId].
     *
     * @param stationId The gas station ID.
     * @param since Must be less than now and not more than 1 year ago.
     * @param granularity Number&Unit (m,d,w,M,y); Example: 15m.
     * @param forecast Determines if the response includes a price forecast.
     * @param completion Returns a list of [PriceHistory] objects on success or a [Throwable] on failure.
     */
    @JvmOverloads
    fun getPriceHistoryByStation(stationId: String, since: Date, granularity: String, forecast: Boolean = false, completion: (Completion<List<PriceHistory>>) -> Unit) {
        priceHistoryApi.getPricesByStation(stationId, since, granularity, forecast, completion)
    }

    /**
     * Returns the price history for the specified [gas station][stationId] and [fuel type][fuelType].
     *
     * @param stationId The gas station ID.
     * @param fuelType Fuel type for cars, based on the EU fuel marking.
     * @param since Must be less than now and not more than 1 year ago.
     * @param granularity Number&Unit (m,d,w,M,y); Example: 15m.
     * @param forecast Determines if the response includes a price forecast.
     * @param completion Returns a list of [PriceHistoryFuelType] objects on success or a [Throwable] on failure.
     */
    @JvmOverloads
    fun getPriceHistoryByStation(stationId: String, fuelType: String, since: Date, granularity: String, forecast: Boolean = false, completion: (Completion<List<PriceHistoryFuelType>>) -> Unit) {
        priceHistoryApi.getPricesByStation(stationId, fuelType, since, granularity, forecast, completion)
    }

    fun getGasStation(id: String, compileOpeningHours: Boolean, forMovedGasStation: Boolean, completion: (Completion<GasStationMovedResponse>) -> Unit) {
        API.gasStations.getGasStation(id, compileOpeningHours).enqueue {
            onResponse = {
                when (it.code()) {
                    GasStationCodes.STATUS_MOVED -> {
                        val newUuid: String? = it.headers().values(GasStationCodes.HEADER_LOCATION).first()?.split("/")?.last()
                        if (newUuid.isNotNullOrEmpty()) {
                            completion(Success(GasStationMovedResponse(newUuid, true, null, null)))
                        } else {
                            completion(Success(GasStationMovedResponse(null, true, null, null)))
                        }
                    }
                    GasStationCodes.STATUS_OK -> {
                        val priorResponse = it.raw().priorResponse
                        if (priorResponse != null) {
                            val newUuid = priorResponse.headers.values(GasStationCodes.HEADER_LOCATION).firstOrNull()?.split("/")?.lastOrNull()
                            if (newUuid.isNotNullOrEmpty()) {
                                completion(Success(GasStationMovedResponse(newUuid, true, null, null)))
                            } else {
                                completion(Success(GasStationMovedResponse(null, false, null, null)))
                            }
                        } else {
                            if (forMovedGasStation)
                                completion(
                                    Success(
                                        GasStationMovedResponse(
                                            null, true, it.body()?.latitude?.toDouble(),
                                            it.body()?.longitude?.toDouble()
                                        )
                                    )
                                )
                            else
                                completion(Success(GasStationMovedResponse(null, false, null, null)))
                        }
                    }
                    GasStationCodes.STATUS_NOT_FOUND -> {
                        completion(Success(GasStationMovedResponse(null, true, null, null)))
                    }
                    else -> {
                        completion(Failure(Exception("Server error")))
                    }
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
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

    /**
     * Returns a list of all Connected Fueling gas stations.
     *
     * @param completion Returns a list of [CofuGasStation]s on success or a [Throwable] on failure.
     */
    fun requestCofuGasStations(completion: (Completion<List<CofuGasStation>>) -> Unit) {
        geoApiManager.cofuGasStations { result ->
            result.onSuccess { completion(Success(it)) }
            result.onFailure { completion(Failure(it)) }
        }
    }

    /**
     * Returns a list of Connected Fueling gas stations within the [radius] of the specified [location].
     *
     * @param location The center of the search radius.
     * @param radius The search radius in meters.
     * @param completion Returns a list of [GasStation]s where Connected Fueling is available on success or a [Throwable] on failure.
     */
    fun requestCofuGasStations(location: Location, radius: Int, completion: (Completion<List<GasStation>>) -> Unit) {
        geoApiManager.cofuGasStations(location, radius) { result ->
            result.onSuccess { completion(Success(it)) }
            result.onFailure { completion(Failure(it)) }
        }
    }

    /**
     * Checks if there is at least one app for the given [poiId] at the current location.
     *
     * @param location Can be specified optionally if the current location should not be determined.
     *
     * @return True if POI with [poiId] is in range, false otherwise.
     */
    suspend fun isPoiInRange(poiId: String, location: Location? = null): Boolean {
        return geoApiManager.isPoiInRange(poiId, location)
    }
}
