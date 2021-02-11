package cloud.pace.sdk

import android.content.Context
import cloud.pace.sdk.api.API
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.KoinConfig

object PACECloudSDK {

    internal lateinit var configuration: Configuration
    var additionalQueryParams: Map<String, String> = mapOf()

    /**
     * Sets up [PACECloudSDK] with the passed [configuration].
     * This needs to be called before any of its "Kits" can be used.
     *
     * @param context The context.
     */
    fun setup(context: Context, configuration: Configuration) {
        this.configuration = configuration

        API.setup(configuration.environment.apiUrl, configuration.apiKey)
        AppKit.locationAccuracy = configuration.locationAccuracy
        KoinConfig.setupCloudSDK(context, configuration.environment, configuration.apiKey)
        AppKit.updateUserAgent()
    }

    /**
     * Replaces the list of optional [extensions] at the end of the user agent (separated with a space).
     */
    fun setUserAgentExtensions(extensions: List<String>) {
        if (::configuration.isInitialized) {
            configuration.extensions = extensions
            AppKit.updateUserAgent()
        }
    }

    internal fun setLocationAccuracy(locationAccuracy: Int?) {
        if (::configuration.isInitialized) {
            configuration.locationAccuracy = locationAccuracy
        }
    }
}
