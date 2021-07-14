package cloud.pace.sdk.poikit

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.gasStations
import cloud.pace.sdk.api.poi.POIAPI.metadataFilters
import cloud.pace.sdk.api.poi.POIAPI.priceHistories
import cloud.pace.sdk.api.poi.POIAPI.prices
import cloud.pace.sdk.api.poi.generated.model.Categories
import cloud.pace.sdk.api.poi.generated.model.Fuel
import cloud.pace.sdk.api.poi.generated.model.PriceHistory
import cloud.pace.sdk.api.poi.generated.model.RegionalPrices
import cloud.pace.sdk.api.poi.generated.request.gasStations.GetGasStationAPI.getGasStation
import cloud.pace.sdk.api.poi.generated.request.metadataFilters.GetMetadataFiltersAPI.getMetadataFilters
import cloud.pace.sdk.api.poi.generated.request.priceHistories.GetPriceHistoryAPI.getPriceHistory
import cloud.pace.sdk.api.poi.generated.request.prices.GetRegionalPricesAPI.getRegionalPrices
import cloud.pace.sdk.poikit.database.POIKitDatabase
import cloud.pace.sdk.poikit.poi.*
import cloud.pace.sdk.poikit.poi.download.TileDownloader
import cloud.pace.sdk.poikit.routing.NavigationApiClient
import cloud.pace.sdk.poikit.routing.NavigationMode
import cloud.pace.sdk.poikit.routing.NavigationRequest
import cloud.pace.sdk.poikit.routing.Route
import cloud.pace.sdk.poikit.search.AddressSearchClient
import cloud.pace.sdk.poikit.search.AddressSearchRequest
import cloud.pace.sdk.poikit.search.PhotonResult
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.poikit.utils.GasStationCodes
import cloud.pace.sdk.poikit.utils.GasStationMovedResponse
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.utils.*
import com.google.android.gms.maps.model.VisibleRegion
import io.reactivex.rxjava3.core.Observable
import org.koin.core.inject
import java.util.*

object POIKit : CloudSDKKoinComponent, LifecycleObserver {

    private val database: POIKitDatabase by inject()
    private val navigationApi: NavigationApiClient by inject()
    private val addressSearchApi: AddressSearchClient by inject()
    private val locationProvider: LocationProvider by inject()
    private val tileDownloader: TileDownloader by inject()

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
    fun observe(visibleRegion: VisibleRegion, padding: Double = 0.0, completion: (Completion<List<PointOfInterest>>) -> Unit): VisibleRegionNotificationToken {
        return VisibleRegionNotificationToken(visibleRegion, padding, database.gasStationDao(), completion)
    }

    fun observe(vararg ids: String, completion: (Completion<List<PointOfInterest>>) -> Unit): IDsNotificationToken {
        return IDsNotificationToken(ids.toList(), database.gasStationDao(), completion)
    }

    fun observe(locations: Map<String, LocationPoint>, completion: (Completion<List<PointOfInterest>>) -> Unit): LocationsNotificationToken {
        return LocationsNotificationToken(locations, database.gasStationDao(), completion)
    }

    @JvmOverloads
    fun requestGasStations(locations: Map<String, LocationPoint>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL, completion: (Completion<List<GasStation>>) -> Unit) {
        onBackgroundThread {
            val tileRequest = locations.values.toTileQueryRequest(zoomLevel)

            tileDownloader.load(tileRequest) {
                it.onSuccess { stations ->
                    stations.forEach { station -> station.updatedAt = Date() }
                    database.gasStationDao().insertGasStations(stations)
                    onMainThread {
                        completion(Success(stations))
                    }
                }

                it.onFailure { error ->
                    onMainThread {
                        completion(Failure(error))
                    }
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

    fun getRegionalPrice(latitude: Double, longitude: Double, completion: (Completion<RegionalPrices?>) -> Unit) {
        API.prices.getRegionalPrices(latitude.toFloat(), longitude.toFloat()).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    completion(Failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }

    fun searchAddress(request: AddressSearchRequest): Observable<PhotonResult> {
        return addressSearchApi.searchAddress(request)
    }

    fun getDynamicFilters(latitude: Double, longitude: Double, completion: (Completion<Categories?>) -> Unit) {
        API.metadataFilters.getMetadataFilters(latitude.toFloat(), longitude.toFloat()).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    completion(Failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }

    fun getPriceHistory(id: String, fuelType: Fuel, from: Date, to: Date, completion: (Completion<PriceHistory?>) -> Unit) {
        API.priceHistories.getPriceHistory(id, fuelType, from, to).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    completion(Failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
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
                        val priorResponse = it.raw().priorResponse()
                        if (priorResponse != null) {
                            val newUuid = priorResponse.headers().values(GasStationCodes.HEADER_LOCATION).first()?.split("/")?.last()
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
}
