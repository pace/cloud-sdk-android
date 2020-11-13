package cloud.pace.sdk.appkit.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import car.pace.cloudsdk.api.poi.LocationBasedApp
import cloud.pace.sdk.appkit.app.api.AppCloudApi
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.model.Car
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationState
import cloud.pace.sdk.utils.SystemManager
import com.google.android.gms.location.FusedLocationProviderClient
import org.mockito.Mockito.mock
import java.util.*

open class TestLocationProvider(private val mockedLocationState: LocationState, private val mockedLocation: Location?) : LocationProvider {

    override val locationState = MutableLiveData<LocationState>()
    override val location = MutableLiveData<Location>()

    override fun requestLocationUpdates() {
        locationState.value = mockedLocationState
        location.value = mockedLocation
    }

    override fun getLastKnownLocation(completion: (Location?) -> Unit) {
        completion(mockedLocation)
    }

    override fun removeLocationUpdates() {}
}

open class TestAppLocationManager(private val location: Location? = null, private val throwable: Throwable? = null) : AppLocationManager {

    override fun getLocation(callback: (Result<Location>) -> Unit) {
        if (location != null) {
            callback(Result.success(location))
        } else if (throwable != null) {
            callback(Result.failure(throwable))
        }
    }
}

open class TestAppRepository : AppRepository {

    override fun getAppsByUrl(context: Context, url: String, references: List<String>, completion: (Result<List<App>>) -> Unit) {}
    override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, retry: Boolean, completion: (Result<List<App>>) -> Unit) {}
    override fun getAllApps(context: Context, retry: Boolean, completion: (Result<List<App>>) -> Unit) {}
    override fun getAppsById(context: Context, uuid: String, references: List<String>, completion: (Result<List<App>>) -> Unit) {}
    override fun getUrlById(uuid: String, completion: (Result<String?>) -> Unit) {}
}

open class TestSharedPreferencesModel(private val appStates: List<SharedPreferencesModel.AppState> = listOf()) : SharedPreferencesModel {

    override fun getCar(): Car {
        return Car("", "", 0, 0)
    }

    override fun getInt(key: String, defValue: Int?): Int? {
        return null
    }

    override fun getLong(key: String, defValue: Long?): Long? {
        return null
    }

    override fun getAppStates(): List<SharedPreferencesModel.AppState> {
        return appStates
    }

    override fun getString(key: String, defValue: String?): String? {
        return null
    }

    override fun getStringSet(key: String, defValues: HashSet<String>?): HashSet<String>? {
        return null
    }

    override fun deleteAppState(url: String) {}
    override fun deleteAllAppStates() {}
    override fun putInt(key: String, value: Int) {}
    override fun putLong(key: String, value: Long) {}
    override fun putString(key: String, value: String) {}
    override fun putStringSet(key: String, values: HashSet<String>) {}
    override fun remove(key: String) {}
    override fun saveAppState(appState: SharedPreferencesModel.AppState) {}
    override fun setCar(car: Car) {}
}

open class TestUriUtils(private val id: String? = null, private val startUrl: String = "") : UriManager {

    override fun getURI(baseUrl: String, parameters: Map<String, String>): String {
        var url = baseUrl
        var firstParameter = true
        for (parameter in parameters) {
            if (firstParameter) {
                firstParameter = false
                url += "/${parameter.key}=${parameter.value}"
            } else {
                url += "&${parameter.key}=${parameter.value}"
            }
        }
        return url
    }

    override fun getStartUrls(baseUrl: String, manifestUrl: String, sdkStartUrl: String?, references: List<String>?): Map<String?, String> {
        return mapOf(id to startUrl)
    }

    override fun buildUrl(baseUrl: String, path: String): String {
        return ""
    }
}

open class TestAppEventManager : AppEventManager {

    override val appDrawerInfo = MutableLiveData<AppEventManager.AppDrawerInfo>()
    override val invalidApps = MutableLiveData<List<String>>()
    override val disabledHost = MutableLiveData<String>()
    override val redirectUrl = MutableLiveData<Event<String>>()

    override fun onAppDrawerChanged(url: String, title: String?, subtitle: String?) {
        appDrawerInfo.value = AppEventManager.AppDrawerInfo(url, title, subtitle)
    }

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

    override fun getBiometricStatus(redirectUri: String?, state: String?) {}
    override fun saveTotpSecret(request: AppWebViewClient.WebClientCallback.TotpSecretRequest) {}
    override fun getTotp(host: String?, key: String?, serverTime: Long?, redirectUri: String?, state: String?) {}
    override fun setSecureData(host: String?, key: String?, value: String?, redirectUri: String?, state: String?) {}
    override fun getSecureData(host: String?, key: String?, redirectUri: String?, state: String?) {}
    override fun setDisableTime(host: String?, until: Long?) {}
    override fun openInNewTab(url: String, cancelUrl: String) {}
    override fun close(reopenRequest: AppWebViewClient.WebClientCallback.ReopenRequest?) {}
    override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {}
    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onCustomSchemeError(context: Context?, cancelUrl: String, scheme: String) {}
}

open class TestAppCloudApi : AppCloudApi {

    override fun getAllApps(retry: Boolean, completion: (Result<Array<LocationBasedApp>?>) -> Unit) {}
    override fun getAppByUuid(uuid: String, completion: (Result<LocationBasedApp?>) -> Unit) {}
    override fun getLocationBasedApps(latitude: Double, longitude: Double, retry: Boolean, completion: (Result<Array<LocationBasedApp>?>) -> Unit) {}
}

open class TestCacheModel : CacheModel {

    override fun getUri(context: Context, url: String, completion: (Result<ByteArray>) -> Unit) {}
    override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {}
}
