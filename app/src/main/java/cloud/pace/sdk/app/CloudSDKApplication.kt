package cloud.pace.sdk.app

import android.app.Application
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.idkit.model.TokenExchangeConfiguration
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment

class CloudSDKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val oidConfiguration = if (BuildConfig.TOKEN_EXCHANGE) {
            CustomOIDConfiguration(
                authorizationEndpoint = "https://id.dev.pace.cloud/auth/realms/MultiRealm/protocol/openid-connect/auth",
                tokenEndpoint = "https://id.dev.pace.cloud/auth/realms/MultiRealm/protocol/openid-connect/token",
                endSessionEndpoint = "https://id.dev.pace.cloud/auth/realms/MultiRealm/protocol/openid-connect/logout",
                redirectUri = "cloud-sdk-example://callback",
                clientSecret = "YIUXbpLZeN6OD1afjXwD4lFZigQAIHp7",
                tokenExchangeConfig = TokenExchangeConfiguration(
                    issuerId = "multi-oidc",
                    clientId = "cloud-sdk-example-app-token-exchange",
                    clientSecret = "IMqeEWNd91lOf9tCEnIFZyOwcnDNV6Jw"
                )
            )
        } else {
            CustomOIDConfiguration(redirectUri = "cloud-sdk-example://callback")
        }

        PACECloudSDK.setup(
            this,
            Configuration(
                clientId = "cloud-sdk-example-app",
                clientAppName = "PACECloudSDKExample",
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "YOUR_API_KEY",
                environment = Environment.DEVELOPMENT,
                oidConfiguration = oidConfiguration
            )
        )
    }
}
