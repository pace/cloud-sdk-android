package car.pace.cofu

import android.app.Application
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.utils.AuthenticationMode
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val environment = Environment.SANDBOX

        PACECloudSDK.setup(
            this, Configuration(
                clientAppName = BuildConfig.PACE_APP_NAME,
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = BuildConfig.CLOUD_API_KEY,
                authenticationMode = AuthenticationMode.NATIVE,
                environment = environment,
                domainACL = listOf(BuildConfig.DOMAIN_ACL),
                geoAppsScope = BuildConfig.GEO_APPS_SCOPE
            )
        )

        IDKit.setup(
            this, OIDConfiguration(
                authorizationEndpoint = "${environment.idUrl}/auth/realms/pace/protocol/openid-connect/auth",
                tokenEndpoint = "${environment.idUrl}/auth/realms/pace/protocol/openid-connect/token",
                endSessionEndpoint = "${environment.idUrl}/auth/realms/pace/protocol/openid-connect/logout",
                userInfoEndpoint = "${environment.idUrl}/auth/realms/pace/protocol/openid-connect/userinfo",
                clientId = BuildConfig.PACE_CLIENT_ID,
                redirectUri = BuildConfig.PACE_REDIRECT_URL
            )
        )
    }
}
