package cloud.pace.sdk.fueling_app

import android.app.Application
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FuelingApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Apply dynamic colors (Material You)
        DynamicColors.applyToActivitiesIfAvailable(this)

        // Setup the PACE Cloud SDK before calling any methods of the SDK
        PACECloudSDK.setup(
            this,
            Configuration(
                clientAppName = "PACECloudSDKFuelingExample",
                clientAppVersion = BuildConfig.VERSION_NAME,
                clientAppBuild = BuildConfig.VERSION_CODE.toString(),
                apiKey = "YOUR_API_KEY",
                environment = Environment.DEVELOPMENT, // Change to Environment.PRODUCTION for production builds
                oidConfiguration = CustomOIDConfiguration(clientId = "cloud-sdk-example-app", redirectUri = "cloud-sdk-example://callback") // Change to your OIDC configuration
            )
        )
    }
}
