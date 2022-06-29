package cloud.pace.sdk

import android.content.Context
import android.util.Log
import cloud.pace.sdk.api.API
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.idkit.model.oidConfiguration
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.ErrorListener
import cloud.pace.sdk.utils.KoinConfig
import cloud.pace.sdk.utils.SetupLogger
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PACECloudSDK {

    internal lateinit var configuration: Configuration
    var isSetup: Boolean = false

    /**
     * The setter replaces the additional query parameters which are appended to each request.
     * It also updates the additional parameters of the [CustomOIDConfiguration] of the [IDKit].
     */
    var additionalQueryParams: Map<String, String> = mapOf()
        set(value) {
            val newQueryParams = value.toMutableMap()

            if (::configuration.isInitialized) {
                // Set the default params
                val defaultUtmParams = mapOf("utm_source" to configuration.clientAppName)
                for ((key, defaultValue) in defaultUtmParams) {
                    if (!newQueryParams.containsKey(key)) {
                        newQueryParams[key] = defaultValue
                    }
                }
            }

            field = newQueryParams

            if (IDKit.isInitialized) {
                // Set the parameters to the same values as before, since they will be merged with the updated additionalQueryParams in the AuthorizationManager (values from the latter map take precedence)
                IDKit.setAdditionalParameters((IDKit.getAdditionalParameters() ?: emptyMap()))
            }
        }

    private var loggingListener: ((String) -> Unit)? = null
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)
    private val cloudSDKTree: Timber.DebugTree by lazy {
        object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                val timestamp = simpleDateFormat.format(Date())
                val logLevel = when (priority) {
                    Log.DEBUG, Log.INFO -> "info"
                    Log.WARN -> "warning"
                    Log.ERROR, Log.ASSERT -> "error"
                    else -> "verbose"
                }
                loggingListener?.invoke("[$timestamp][$tag][$logLevel] $message")
            }
        }
    }

    /**
     * `true`, if the logging should be enabled, `false` otherwise (default: `false`).
     */
    var isLoggingEnabled = false
        set(value) {
            field = value
            if (value) {
                if (!Timber.forest().contains(cloudSDKTree)) {
                    Timber.plant(cloudSDKTree)
                }
            } else {
                if (Timber.forest().contains(cloudSDKTree)) {
                    Timber.uproot(cloudSDKTree)
                }
            }
        }

    /**
     * Sets up [PACECloudSDK] with the passed [configuration].
     * This needs to be called before any of its "Kits" can be used.
     *
     * @param context The context.
     */
    fun setup(context: Context, configuration: Configuration) {
        this.configuration = configuration

        // Indicates that setup function has been called for logging purposes
        isSetup = true

        // Call setter of additionalQueryParams to set the default params
        additionalQueryParams = additionalQueryParams

        // Do not log to logcat on production
        if (configuration.environment != Environment.PRODUCTION) {
            Timber.plant(Timber.DebugTree())
        }

        KoinConfig.setupCloudSDK(context, configuration)

        val oidConfiguration = configuration.oidConfiguration
        if (oidConfiguration != null) {
            IDKit.setup(context, oidConfiguration.oidConfiguration(configuration.environment))
        }

        API.setup(configuration.environment, configuration.apiKey)

        AppKit.locationAccuracy = configuration.locationAccuracy
        AppKit.updateUserAgent()

        SetupLogger.apiKey = configuration.apiKey
        SetupLogger.redirectScheme = DeviceUtils.getPACERedirectScheme(context)
        SetupLogger.environment = configuration.environment
        SetupLogger.domainACL = configuration.domainACL
        SetupLogger.checkRedirectScheme = configuration.checkRedirectScheme

        SetupLogger.preCheckSetup()
    }

    /**
     * Sets the logging listener to [loggingListener].
     */
    fun setLoggingListener(loggingListener: (String) -> Unit) {
        this.loggingListener = loggingListener
    }

    /**
     * Set the error listener to [errorListener].
     */
    fun setErrorListener(errorListener: ErrorListener?) {
        ErrorListener.errorListener = errorListener
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
