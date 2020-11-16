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
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.location.AppLocationManagerImpl
import cloud.pace.sdk.appkit.network.NetworkChangeListener
import cloud.pace.sdk.appkit.network.NetworkChangeListenerImpl
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.pay.PayAuthenticationManagerImpl
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.appkit.persistence.CacheModelImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.poikit.database.POIKitDatabase
import cloud.pace.sdk.poikit.poi.download.DynamicFilterApiClient
import cloud.pace.sdk.poikit.poi.download.GasStationApiClient
import cloud.pace.sdk.poikit.poi.download.PoiApiClient
import cloud.pace.sdk.poikit.poi.download.PriceHistoryApiClient
import cloud.pace.sdk.poikit.routing.NavigationApiClient
import cloud.pace.sdk.poikit.search.AddressSearchClient
import com.google.android.gms.location.LocationServices
import net.openid.appauth.AuthorizationService
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

object KoinConfig {

    internal lateinit var idKitKoinApp: KoinApplication
    internal lateinit var appKitKoinApp: KoinApplication
    internal lateinit var poiKitKoinApp: KoinApplication

    @Synchronized
    internal fun setupIDKit(context: Context) {
        idKitKoinApp = koinApplication {
            androidContext(context)
            modules(module {
                single { AuthorizationService(get()) }
            })
        }
    }

    @Synchronized
    internal fun setupAppKit(context: Context) {
        appKitKoinApp = koinApplication {
            androidContext(context)
            modules(module {
                single<SharedPreferencesModel> { SharedPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(get())) }
                single<CacheModel> { CacheModelImpl() }
                single<AppRepository> { AppRepositoryImpl(get(), get(), get(), get()) }
                single<NetworkChangeListener> { NetworkChangeListenerImpl(get()) }
                single<AppEventManager> { AppEventManagerImpl() }
                single<PayAuthenticationManager> { PayAuthenticationManagerImpl(get()) }
                single<UriManager> { UriManagerImpl() }
                single<SystemManager> { SystemManagerImpl(get()) }
                single<LocationProvider> { LocationProviderImpl(get(), get()) }
                single<AppLocationManager> { AppLocationManagerImpl(get(), get()) }
                single<AppCloudApi> { AppCloudApiImpl() }
                single { GeofenceCallback() }
                single { LocationServices.getGeofencingClient(get<Context>()) }
                single<AppModel> { AppModelImpl() }
                single { AppManager() }
                viewModel<AppFragmentViewModel> { AppFragmentViewModelImpl(get(), get()) }
                viewModel<AppWebViewModel> { AppWebViewModelImpl(get(), get(), get(), get(), get()) }
                viewModel<AppDrawerViewModel> { AppDrawerViewModelImpl(get()) }
            })
        }
    }

    @Synchronized
    internal fun setupPOIKit(context: Context, environment: Environment, deviceId: String) {
        poiKitKoinApp = koinApplication {
            androidContext(context)
            modules(module {
                single {
                    Room.databaseBuilder(get(), POIKitDatabase::class.java, POIKitDatabase.DATABASE_NAME)
                        .addMigrations(POIKitDatabase.migration1to2, POIKitDatabase.migration2to3, POIKitDatabase.migration3to4, POIKitDatabase.migration4to5)
                        .build()
                }
                single { NavigationApiClient(environment) }
                single { PoiApiClient(environment) }
                single { AddressSearchClient(environment) }
                single { DynamicFilterApiClient(environment) }
                single { PriceHistoryApiClient(environment, deviceId) }
                single { GasStationApiClient(environment, deviceId) }
                single<SystemManager> { SystemManagerImpl(get()) }
                single<LocationProvider> { LocationProviderImpl(get(), get()) }
            })
        }
    }

    fun setupForTests(context: Context, module: Module) {
        appKitKoinApp = koinApplication {
            androidContext(context)
            modules(module)
        }
    }
}
