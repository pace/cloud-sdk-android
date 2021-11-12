package cloud.pace.sdk.utils

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.room.Room
import cloud.pace.sdk.appkit.AppManager
import cloud.pace.sdk.appkit.app.AppFragmentViewModel
import cloud.pace.sdk.appkit.app.AppFragmentViewModelImpl
import cloud.pace.sdk.appkit.app.api.*
import cloud.pace.sdk.appkit.app.drawer.AppDrawerViewModel
import cloud.pace.sdk.appkit.app.drawer.AppDrawerViewModelImpl
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModelImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppEventManagerImpl
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.geofences.GeofenceCallback
import cloud.pace.sdk.appkit.network.NetworkChangeListener
import cloud.pace.sdk.appkit.network.NetworkChangeListenerImpl
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.pay.PayAuthenticationManagerImpl
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.appkit.persistence.CacheModelImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.idkit.authorization.AuthorizationManager
import cloud.pace.sdk.idkit.credentials.CredentialsManager
import cloud.pace.sdk.poikit.database.POIKitDatabase
import cloud.pace.sdk.poikit.geo.GeoAPIClient
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl
import cloud.pace.sdk.poikit.poi.download.TileDownloader
import cloud.pace.sdk.poikit.pricehistory.PriceHistoryClient
import cloud.pace.sdk.poikit.routing.NavigationApiClient
import cloud.pace.sdk.poikit.search.AddressSearchClient
import com.google.android.gms.location.LocationServices
import net.openid.appauth.AuthorizationService
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

object KoinConfig {

    internal lateinit var cloudSDKKoinApp: KoinApplication

    @Synchronized
    internal fun setupCloudSDK(context: Context, environment: Environment, apiKey: String) {
        cloudSDKKoinApp = koinApplication {
            androidContext(context)
            modules(module {
                factory<LocationProvider> { LocationProviderImpl(get(), get()) }
                single {
                    Room.databaseBuilder(get(), POIKitDatabase::class.java, POIKitDatabase.DATABASE_NAME)
                        .addMigrations(POIKitDatabase.migration1to2, POIKitDatabase.migration2to3, POIKitDatabase.migration3to4, POIKitDatabase.migration4to5, POIKitDatabase.migration5to6)
                        .build()
                }
                single { TileDownloader(environment) }
                single { NavigationApiClient(environment, apiKey) }
                single { AddressSearchClient(environment, apiKey) }
                single { GeoAPIClient(environment, get()) }
                single { PriceHistoryClient(environment) }
                single<SystemManager> { SystemManagerImpl(get()) }
                single<SharedPreferencesModel> { SharedPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(get())) }
                single<CacheModel> { CacheModelImpl() }
                single<AppRepository> { AppRepositoryImpl(get(), get(), get(), get(), get()) }
                single<NetworkChangeListener> { NetworkChangeListenerImpl(get()) }
                single<AppEventManager> { AppEventManagerImpl() }
                single<PayAuthenticationManager> { PayAuthenticationManagerImpl(get()) }
                single<UriManager> { UriManagerImpl() }
                single<AppAPI> { AppAPIImpl(get()) }
                single { GeofenceCallback() }
                single { LocationServices.getGeofencingClient(get<Context>()) }
                single<AppModel> { AppModelImpl(get()) }
                single { AppManager(DefaultDispatcherProvider()) }
                single<GeoAPIManager>(createdAtStart = true) { GeoAPIManagerImpl(get(), get(), get()) }
                single { AuthorizationService(get()) }
                single { AuthorizationManager(get(), get(), get()) }
                single { CredentialsManager(get(), get(), get()) }
                viewModel<AppFragmentViewModel> { AppFragmentViewModelImpl(get(), get()) }
                viewModel<AppWebViewModel> { (context: Context) -> AppWebViewModelImpl(context, get(), get(), get(), get(), get()) }
                viewModel<AppDrawerViewModel> { AppDrawerViewModelImpl(get()) }
            })
        }
    }

    fun setupForTests(context: Context, module: Module) {
        cloudSDKKoinApp = koinApplication {
            androidContext(context)
            modules(module)
        }
    }
}
