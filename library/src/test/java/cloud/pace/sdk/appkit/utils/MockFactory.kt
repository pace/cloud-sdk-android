package cloud.pace.sdk.appkit.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApp
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApps
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.poikit.geo.CofuGasStation
import cloud.pace.sdk.poikit.geo.GeoAPIFeature
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.geo.GeoAPIResponse
import cloud.pace.sdk.poikit.geo.GeoGasStation
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationState
import cloud.pace.sdk.utils.NoLocationFound
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.SystemManager
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.mockk
import org.mockito.Mockito.mock

open class TestLocationProvider(
    private val mockedLocation: Location? = null,
    private val throwable: Throwable = NoLocationFound,
    private val mockedLocationState: LocationState = LocationState.LOCATION_HIGH_ACCURACY
) : LocationProvider {

    override val locationState = MutableLiveData<LocationState>()
    override val location = MutableLiveData<Location>()

    override fun requestLocationUpdates() {
        locationState.value = mockedLocationState
        location.value = mockedLocation
    }

    override fun removeLocationUpdates() {}

    override suspend fun firstValidLocation(timeout: Long): Completion<Location> {
        return mockedLocation?.let { Success(it) } ?: Failure(throwable)
    }

    override suspend fun currentLocation(validate: Boolean, timeout: Long): Completion<Location?> {
        return mockedLocation?.let { Success(it) } ?: Failure(throwable)
    }

    override suspend fun lastKnownLocation(validate: Boolean, timeout: Long): Completion<Location?> {
        return mockedLocation?.let { Success(it) } ?: Failure(throwable)
    }
}

open class TestAppRepository : AppRepository {

    override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
        return Success(emptyList())
    }

    override suspend fun getAllApps(): Completion<List<App>> {
        return Success(emptyList())
    }

    override suspend fun getAppsByUrl(url: String, references: List<String>): Completion<List<App>> {
        return Success(emptyList())
    }

    override suspend fun getUrlByAppId(appId: String): Completion<String?> {
        return Success(null)
    }

    override fun getFuelingUrl(poiId: String, completion: (String) -> Unit) {}
}

open class TestSharedPreferencesModel : SharedPreferencesModel {

    override fun getInt(key: String, defValue: Int?): Int? {
        return null
    }

    override fun getLong(key: String, defValue: Long?): Long? {
        return null
    }

    override fun getString(key: String, defValue: String?): String? {
        return null
    }

    override fun getStringSet(key: String, defValues: HashSet<String>?): HashSet<String>? {
        return null
    }

    override fun getBoolean(key: String, defValue: Boolean?): Boolean? {
        return null
    }

    override fun migrateUserValuesToUserId() {}
    override fun putBoolean(key: String, value: Boolean) {}
    override fun putInt(key: String, value: Int) {}
    override fun putLong(key: String, value: Long) {}
    override fun putString(key: String, value: String) {}
    override fun putStringSet(key: String, values: HashSet<String>) {}
    override fun remove(key: String) {}
    override fun setCar(car: Car) {}
    override fun getCar(): Car {
        return Car("", "", 0, 0)
    }

    override fun setTotpSecret(host: String?, key: String, totpSecret: TotpSecret) {}

    override fun getTotpSecret(host: String?, key: String): TotpSecret? {
        return null
    }

    override fun removeTotpSecret(host: String?, key: String) {}
}

open class TestUriUtils(private val id: String? = null, private val startUrl: String = "") : UriManager {

    override fun getStartUrls(baseUrl: String, references: List<String>?): Map<String?, String> {
        return mapOf(id to startUrl)
    }

    override fun getStartUrl(baseUrl: String, reference: String?): String {
        return startUrl
    }

    override fun appendPath(baseUrl: String, path: String): String {
        return ""
    }

    override fun appendQueryParameter(baseUrl: String, key: String, value: String): String {
        return ""
    }

    override fun getIconUrl(baseUrl: String, url: String): String {
        return ""
    }
}

open class TestAppEventManager : AppEventManager {

    override val invalidApps = MutableLiveData<List<String>>()
    override val disabledHost = MutableLiveData<String>()

    override fun setInvalidApps(list: List<String>) {
        invalidApps.value = list
    }

    override fun setDisabledHost(host: String) {
        disabledHost.value = host
    }
}

open class TestSystemManager(
    private val locationPermissionsGranted: Boolean = true,
    private val isGooglePlayServicesAvailable: Boolean = true,
    private val mockFusedLocationProviderClient: FusedLocationProviderClient = mock(FusedLocationProviderClient::class.java),
    private val mockLocationManager: LocationManager = mock(LocationManager::class.java),
    private val mockConnectivityManager: ConnectivityManager = mock(ConnectivityManager::class.java),
    private val mockHandler: Handler = mock(Handler::class.java),
    private val mockTimeMillis: Long = System.currentTimeMillis()
) : SystemManager {

    override fun isLocationPermissionGranted(): Boolean {
        return locationPermissionsGranted
    }

    override fun isGooglePlayServicesAvailable(): Boolean {
        return isGooglePlayServicesAvailable
    }

    override fun getFusedLocationProviderClient(): FusedLocationProviderClient {
        return mockFusedLocationProviderClient
    }

    override fun getLocationManager(): LocationManager? {
        return mockLocationManager
    }

    override fun getConnectivityManager(): ConnectivityManager? {
        return mockConnectivityManager
    }

    override fun getHandler(): Handler {
        return mockHandler
    }

    override fun getCurrentTimeMillis(): Long {
        return mockTimeMillis
    }
}

open class TestWebClientCallback : AppWebViewClient.WebClientCallback {

    override fun onClose() {}
    override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {}
    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onUrlChanged(newUrl: String) {}
}

open class TestAppAPI : AppAPI {

    override suspend fun getGeoApiApps(): Result<GeoAPIResponse> = mockk()
    override fun getAllApps(completion: (Result<LocationBasedApps>) -> Unit) {}
    override fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp>) -> Unit) {}
}

open class TestCacheModel : CacheModel {

    override fun getUri(context: Context, url: String, completion: (Result<ByteArray>) -> Unit) {}
    override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {}
}

open class TestGeoAPIManager(private val isPoiInRange: Boolean = false) : GeoAPIManager {

    override suspend fun apps(latitude: Double, longitude: Double): Result<List<GeoGasStation>> = mockk()
    override suspend fun features(latitude: Double, longitude: Double): Result<List<GeoAPIFeature>> = mockk()
    override fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {}
    override fun cofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit) {}
    override suspend fun isPoiInRange(poiId: String, location: Location?) = isPoiInRange
}
