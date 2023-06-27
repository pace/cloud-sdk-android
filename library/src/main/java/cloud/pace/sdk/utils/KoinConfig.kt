package cloud.pace.sdk.utils

import android.content.Context
import cloud.pace.sdk.appkit.AppManager
import cloud.pace.sdk.appkit.app.AppActivityViewModel
import cloud.pace.sdk.appkit.app.AppActivityViewModelImpl
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.appkit.app.api.AppAPIImpl
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.api.AppRepositoryImpl
import cloud.pace.sdk.appkit.app.api.ManifestClient
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.app.api.UriManagerImpl
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawerViewModel
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawerViewModelImpl
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModelImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppEventManagerImpl
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.pay.PayAuthenticationManagerImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.idkit.authorization.AuthorizationManager
import cloud.pace.sdk.idkit.authorization.SessionHolder
import cloud.pace.sdk.idkit.browsermatcher.CustomBrowserMatcher
import cloud.pace.sdk.idkit.credentials.CredentialsManager
import cloud.pace.sdk.idkit.model.oidConfiguration
import cloud.pace.sdk.idkit.userinfo.UserInfoApiClient
import cloud.pace.sdk.poikit.geo.GeoAPIClient
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl
import cloud.pace.sdk.poikit.poi.tiles.POIAPI
import cloud.pace.sdk.poikit.poi.tiles.POIAPIImpl
import cloud.pace.sdk.poikit.poi.tiles.TilesAPIManager
import cloud.pace.sdk.poikit.poi.tiles.TilesAPIManagerImpl
import cloud.pace.sdk.poikit.pricehistory.PriceHistoryClient
import cloud.pace.sdk.poikit.routing.NavigationApiClient
import cloud.pace.sdk.poikit.search.AddressSearchClient
import com.google.android.gms.location.LocationServices
import net.openid.appauth.AppAuthConfiguration
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
    internal fun setupCloudSDK(context: Context, configuration: Configuration) {
        cloudSDKKoinApp = koinApplication {
            androidContext(context)
            modules(
                module {
                    factory<LocationProvider> { LocationProviderImpl(get(), get()) }
                    single { NavigationApiClient(configuration.environment, configuration.apiKey) }
                    single { AddressSearchClient(configuration.environment, configuration.apiKey) }
                    single { GeoAPIClient(configuration.environment, get()) }
                    single { PriceHistoryClient(configuration.environment) }
                    single { UserInfoApiClient(configuration.oidConfiguration?.oidConfiguration(configuration.environment)?.userInfoEndpoint) }
                    single { ManifestClient(get()) }
                    single<SystemManager> { SystemManagerImpl(get()) }
                    single<SharedPreferencesModel> {
                        val sharedPreferencesImpl = SharedPreferencesImpl(context, get())
                        sharedPreferencesImpl.migrateUserValuesToUserId()
                        sharedPreferencesImpl
                    }
                    single<AppRepository> { AppRepositoryImpl(get(), get(), get(), get(), get()) }
                    single<AppEventManager> { AppEventManagerImpl() }
                    single<PayAuthenticationManager> { PayAuthenticationManagerImpl(get()) }
                    single<UriManager> { UriManagerImpl() }
                    single<AppAPI> { AppAPIImpl(get()) }
                    single { LocationServices.getGeofencingClient(get<Context>()) }
                    single<AppModel> { AppModelImpl(get()) }
                    single { AppManager() }
                    single<GeoAPIManager> { GeoAPIManagerImpl(get(), get(), get()) }
                    single { SessionHolder(get()) }
                    single {
                        val appAuthConfig = AppAuthConfiguration.Builder().setBrowserMatcher(CustomBrowserMatcher(get())).build()
                        AuthorizationService(get(), appAuthConfig)
                    }
                    single { AuthorizationManager(get(), get(), get(), get()) }
                    single { CredentialsManager(get(), get(), get()) }
                    single<POIAPI> { POIAPIImpl() }
                    single<TilesAPIManager> { TilesAPIManagerImpl(get()) }
                    single(createdAtStart = true) { MigrationHelper(get(), get()) }
                    viewModel<AppActivityViewModel> { AppActivityViewModelImpl(get()) }
                    viewModel<AppWebViewModel> { (context: Context) -> AppWebViewModelImpl(context, get(), get(), get(), get(), get()) }
                    viewModel<AppDrawerViewModel> { AppDrawerViewModelImpl(get(), get(), get(), get()) }
                }
            )
        }
    }

    fun setupForTests(context: Context, module: Module) {
        cloudSDKKoinApp = koinApplication {
            androidContext(context)
            modules(module)
        }
    }
}
