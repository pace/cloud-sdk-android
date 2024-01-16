package car.pace.cofu

import android.app.Application
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_ENABLED
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.AuthenticationMode
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject

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
    }
}
