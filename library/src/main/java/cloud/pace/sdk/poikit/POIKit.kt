package cloud.pace.sdk.poikit

import android.location.Location
import androidx.lifecycle.*
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.prices
import cloud.pace.sdk.api.poi.generated.model.RegionalPrices
import cloud.pace.sdk.api.poi.generated.request.prices.GetRegionalPricesAPI.getRegionalPrices
import cloud.pace.sdk.poikit.geo.CofuGasStation
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.poi.*
import cloud.pace.sdk.poikit.poi.tiles.TilesAPIManager
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
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.utils.*
import cloud.pace.sdk.utils.LocationProviderImpl.Companion.DEFAULT_LOCATION_REQUEST
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.VisibleRegion
import io.reactivex.rxjava3.core.Observable
import org.koin.core.component.inject
import java.util.*

object POIKit : CloudSDKKoinComponent {

    private val navigationApi: NavigationApiClient by inject()
    private val addressSearchApi: AddressSearchClient by inject()
    private val priceHistoryApi: PriceHistoryClient by inject()
    private val locationProvider: LocationProvider by inject()
    private val geoApiManager: GeoAPIManager by inject()
    private val tilesApiManager: TilesAPIManager by inject()

    /**
     * Checks whether [PACECloudSDK] has been set up correctly before [POIKit] is used, otherwise log SDK warnings.
     */
    init {
        SetupLogger.logSDKWarningIfNeeded()
    }

    fun startLocationListener(locationRequest: LocationRequest = DEFAULT_LOCATION_REQUEST): LocationProvider {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                locationProvider.requestLocationUpdates(locationRequest)
            }

            override fun onStop(owner: LifecycleOwner) {
                stopLocationListener()
            }
        })

        return locationProvider.also { it.requestLocationUpdates(locationRequest) }
    }

    fun stopLocationListener() {
        locationProvider.removeLocationUpdates()
    }

    /**
     * Returns a [Result] of [GasStation]s within the [visibleRegion] plus the provided [padding] at the defined [zoomLevel] on success or a [Throwable] on failure.
     *
     * @param visibleRegion [VisibleRegion] (bounding box) for which [GasStation]s should be returned.
     * @param padding Additional padding around the visible region.
     * @param zoomLevel The zoom level for which gas station information should be retrieved.
     *
     * @return [GasStation]s on success or [Throwable] on failure.
     */
    suspend fun getGasStations(visibleRegion: VisibleRegion, padding: Double = 0.0, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<List<GasStation>> {
        return tilesApiManager.getTiles(visibleRegion, padding, zoomLevel)
    }

    /**
     * Returns a [Result] of [GasStation]s by [ids] at the defined [zoomLevel] on success or a [Throwable] on failure.
     *
     * **Note:** This function first requests the gas stations from the GasStationAPI to get the locations for requesting the tiles.
     * If the locations of the gas stations are known, prefer the variant where you can pass a map of `idsWithLocation` to save bandwidth.
     *
     * @param ids A list of [GasStation] IDs.
     * @param zoomLevel The zoom level for which gas station information should be retrieved.
     *
     * @return [GasStation]s on success or [Throwable] on failure.
     */
    suspend fun getGasStations(ids: List<String>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<List<GasStation>> {
        return tilesApiManager.getTiles(ids, zoomLevel)
    }

    /**
     * Returns a [Result] of [GasStation]s by [idsWithLocations] at the defined [zoomLevel] on success or a [Throwable] on failure.
     * The [idsWithLocations] is a map of the [GasStation] ID and the [GasStation] location.
     *
     * @param idsWithLocations A map of [GasStation] entries where the key is the [GasStation] ID and the value is the [GasStation] location.
     * @param zoomLevel The zoom level for which gas station information should be retrieved.
     *
     * @return [GasStation]s on success or [Throwable] on failure.
     */
    suspend fun getGasStations(idsWithLocations: Map<String, LocationPoint>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<List<GasStation>> {
        return tilesApiManager.getTiles(idsWithLocations, zoomLevel)
    }

    /**
     * Returns a [Result] of a [GasStation] by [id] at the defined [zoomLevel] on success or a [Throwable] on failure.
     *
     * **Note:** This function first requests the gas station from the GasStationAPI to get the location for requesting the tiles.
     * If the location of the gas station is known, prefer the variant where you can pass a pair of ID and location (`idWithLocation`) to save bandwidth.
     *
     * @param id The ID of the [GasStation].
     * @param zoomLevel The zoom level for which gas station information should be retrieved.
     *
     * @return [GasStation] on success or [Throwable] on failure.
     */
    suspend fun getGasStation(id: String, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<GasStation> {
        return tilesApiManager.getTiles(id, zoomLevel)
    }

    /**
     * Returns a [Result] of a [GasStation] by [idWithLocation] at the defined [zoomLevel] on success or a [Throwable] on failure.
     * The [idWithLocation] is a pair of the [GasStation] ID and the [GasStation] location.
     *
     * @param idWithLocation A pair of the [GasStation] ID and the [GasStation] location.
     * @param zoomLevel The zoom level for which gas station information should be retrieved.
     *
     * @return [GasStation] on success or [Throwable] on failure.
     */
    suspend fun getGasStation(idWithLocation: Pair<String, LocationPoint>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<GasStation> {
        return tilesApiManager.getTiles(idWithLocation, zoomLevel)
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
     * Returns a list of Connected Fueling gas stations within the [visibleRegion].
     *
     * @param visibleRegion The [VisibleRegion] to be searched in.
     * @param completion Returns a list of [GasStation]s where Connected Fueling is available on success or a [Throwable] on failure.
     */
    fun requestCofuGasStations(visibleRegion: VisibleRegion, completion: (Completion<List<GasStation>>) -> Unit) {
        geoApiManager.cofuGasStations(visibleRegion) { result ->
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
