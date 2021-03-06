package cloud.pace.sdk.appkit.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.api.geo.CofuGasStation
import cloud.pace.sdk.api.geo.GeoAPIFeature
import cloud.pace.sdk.api.geo.GeoAPIResponse
import cloud.pace.sdk.api.geo.GeoGasStation
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApp
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApps
import cloud.pace.sdk.api.poi.generated.model.LocationBasedAppsWithRefs
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.geo.GeoAPIManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import org.mockito.Mockito.mock
import java.util.*

open class TestLocationProvider(
    private val mockedLocation: Location? = null,
    private val throwable: Throwable = NoLocationFound,
    private val mockedLocationState: LocationState = LocationState.LOCATION_HIGH_ACCURACY,
) : LocationProvider {

    override val locationState = MutableLiveData<LocationState>()
    override val location = MutableLiveData<Location>()

    override fun requestLocationUpdates() {
        locationState.value = mockedLocationState
        location.value = mockedLocation
    }

    override fun removeLocationUpdates() {}

    override suspend fun firstValidLocation(): Completion<Location> {
        return mockedLocation?.let { Success(it) } ?: Failure(throwable)
    }

    override suspend fun currentLocation(validate: Boolean): Completion<Location?> {
        return mockedLocation?.let { Success(it) } ?: Failure(throwable)
    }

    override suspend fun lastKnownLocation(validate: Boolean): Completion<Location?> {
        return mockedLocation?.let { Success(it) } ?: Failure(throwable)
    }
}

open class TestAppRepository(private val isPoiInRange: Boolean = false) : AppRepository {

    override fun getAppsByUrl(context: Context, url: String, references: List<String>, completion: (Result<List<App>>) -> Unit) {}
    override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit) {}
    override fun getAllApps(context: Context, completion: (Result<List<App>>) -> Unit) {}
    override fun getUrlByAppId(appId: String, completion: (Result<String?>) -> Unit) {}
    override fun getCofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {}
    override fun getCofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit) {}
    override suspend fun isPoiInRange(poiId: String, latitude: Double, longitude: Double) = isPoiInRange
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

    override fun getStartUrls(baseUrl: String, manifestUrl: String, sdkStartUrl: String?, references: List<String>?): Map<String?, String> {
        return mapOf(id to startUrl)
    }

    override fun buildUrl(baseUrl: String, path: String): String {
        return ""
    }
}

open class TestAppEventManager : AppEventManager {

    override val invalidApps = MutableLiveData<List<String>>()
    override val disabledHost = MutableLiveData<String>()
    override val redirectUrl = MutableLiveData<Event<String>>()

    override fun setInvalidApps(list: List<String>) {
        invalidApps.value = list
    }

    override fun setDisabledHost(host: String) {
        disabledHost.value = host
    }

    override fun onReceivedRedirect(url: String) {
        redirectUrl.value = Event(url)
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

    override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {}
    override fun getLocationBasedApps(latitude: Double, longitude: Double, completion: (Result<LocationBasedAppsWithRefs>) -> Unit) {}
    override fun getAllApps(completion: (Result<LocationBasedApps>) -> Unit) {}
    override fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp>) -> Unit) {}
}

open class TestCacheModel : CacheModel {

    override fun getUri(context: Context, url: String, completion: (Result<ByteArray>) -> Unit) {}
    override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {}
}

open class TestGeoAPIManager : GeoAPIManager {

    override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {}
    override fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {}
    override fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {}
    override fun cofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit) {}
}
