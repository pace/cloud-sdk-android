package car.pace.cofu

import android.app.Application
import android.os.Build
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_ENABLED
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.AuthenticationMode
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.Environment
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        // Note: This needs to be done first because Sentry SDK can catch errors and crashes only after it is initialized
        if (BuildConfig.SENTRY_ENABLED) {
            // Setup Sentry
            SentryAndroid.init(this) { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.isAttachScreenshot = false
            }
        }

        val analyticsEnabled = BuildConfig.ANALYTICS_ENABLED && sharedPreferencesRepository.getBoolean(PREF_KEY_TRACKING_ENABLED, false)
        Firebase.analytics.setAnalyticsCollectionEnabled(analyticsEnabled)

        PACECloudSDK.setup(
            this,
            Configuration(
                clientId = BuildConfig.CLIENT_ID,
                clientAppName = applicationContext.getString(R.string.app_name),
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "none",
                authenticationMode = AuthenticationMode.NATIVE,
                environment = Environment.PRODUCTION,
                oidConfiguration = CustomOIDConfiguration(
                    redirectUri = BuildConfig.REDIRECT_URI,
                    additionalParameters = BuildConfig.DEFAULT_IDP?.let { mapOf("kc_idp_hint" to it) }
                )
            )
        )

        logAppSessionStart(analyticsEnabled)
    }

    private fun logAppSessionStart(analyticsEnabled: Boolean) {
        // Log a visual separation for the beginning of a new app session
        val icons = "\uD83D\uDE80\uD83D\uDE80\uD83D\uDE80"
        Timber.d(
            "$icons================================================== App Launch ==================================================$icons\n" +
                "App Version: ${BuildConfig.VERSION_NAME}\n" +
                "OS Version: ${Build.VERSION.RELEASE}\n" +
                "Device: ${DeviceUtils.getDeviceName()}\n" +
                "App Language: ${Locale.getDefault().displayLanguage}\n" +
                "System Region: ${Locale.getDefault().country}\n" +
                "Show onboarding header: ${BuildConfig.ONBOARDING_SHOW_CUSTOM_HEADER}\n" +
                "Show home header: ${BuildConfig.HOME_SHOW_CUSTOM_HEADER}\n" +
                "Show detail icon: ${BuildConfig.DETAIL_SCREEN_SHOW_ICON}\n" +
                "Map enabled: ${BuildConfig.MAP_ENABLED}\n" +
                "Hide prices: ${BuildConfig.HIDE_PRICES}\n" +
                "Sentry enabled: ${BuildConfig.SENTRY_ENABLED}\n" +
                "Firebase enabled: ${BuildConfig.FIREBASE_ENABLED}\n" +
                "Analytics enabled: $analyticsEnabled\n" +
                "Automatic production updates enabled: ${BuildConfig.AUTOMATIC_PRODUCTION_UPDATES_ENABLED}\n" +
                "Native fuelcard management enabled: ${BuildConfig.NATIVE_FUELCARD_MANAGEMENT_ENABLED}\n" +
                "Vehicle integration enabled: ${BuildConfig.VEHICLE_INTEGRATION_ENABLED}\n"
        )
    }
}
