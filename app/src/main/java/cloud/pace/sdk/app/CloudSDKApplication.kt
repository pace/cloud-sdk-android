package cloud.pace.sdk.app

import android.app.Application
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment

class CloudSDKApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        PACECloudSDK.setup(
            this,
            Configuration(
                clientId = "cloud-sdk-example-app",
                clientAppName = "PACECloudSDKExample",
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "YOUR_API_KEY",
                environment = Environment.DEVELOPMENT,
                oidConfiguration = CustomOIDConfiguration(redirectUri = "cloud-sdk-example://callback")
            )
        )
    }
}
