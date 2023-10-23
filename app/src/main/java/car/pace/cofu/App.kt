package car.pace.cofu

import android.app.Application
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.AuthenticationMode
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid

@HiltAndroidApp
class App : Application() {
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

        PACECloudSDK.setup(
            this,
            Configuration(
                clientAppName = applicationContext.getString(R.string.app_name),
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = BuildConfig.CLOUD_API_KEY,
                authenticationMode = AuthenticationMode.NATIVE,
                environment = Environment.DEVELOPMENT,
                geoAppsScope = BuildConfig.PACE_CLIENT_ID,
                oidConfiguration = CustomOIDConfiguration(
                    clientId = BuildConfig.PACE_CLIENT_ID,
                    redirectUri = BuildConfig.PACE_REDIRECT_URL
                )
            )
        )
    }
}
