package cloud.pace.sdk

import android.content.Context
import android.util.Log
import cloud.pace.sdk.api.API
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

object PACECloudSDK {

    internal lateinit var configuration: Configuration
    var isSetup: Boolean = false
    var additionalQueryParams: Map<String, String> = mapOf()
        set(value) {
            val newQueryParams = mutableMapOf<String, String>()
            newQueryParams.putAll(value)

            defaultUtmParams.forEach {
                val oldValue = field[it]
                if (!newQueryParams.containsKey(it) && oldValue != null)
                    newQueryParams[it] = oldValue
            }

            IDKit.setAdditionalParameters(newQueryParams)
            field = newQueryParams
        }

    private val defaultUtmParams = listOf("utm_source")
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

        // Do not log to logcat on production
        if (configuration.environment != Environment.PRODUCTION) {
            Timber.plant(Timber.DebugTree())
        }

        KoinConfig.setupCloudSDK(context, configuration.environment, configuration.apiKey)
        configuration.oidConfiguration?.let {

            IDKit.setup(
                context,
                configuration.environment.getOIDConfiguration(
                    it.clientId,
                    it.clientSecret,
                    it.scopes,
                    it.redirectUri,
                    it.responseType,
                    it.additionalParameters,
                    it.authorizationEndpoint,
                    it.endSessionEndpoint,
                    it.tokenEndpoint,
                    it.userInfoEndpoint
                )
            )
        }

        API.setup(configuration.environment.apiUrl, configuration.apiKey)

        AppKit.locationAccuracy = configuration.locationAccuracy
        AppKit.updateUserAgent()

        isSetup = true

        SetupLogger.apiKey = configuration.apiKey
        SetupLogger.redirectScheme = DeviceUtils.getRedirectScheme(context)
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
