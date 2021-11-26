package cloud.pace.sdk.utils

import cloud.pace.sdk.idkit.model.OIDConfiguration

data class Configuration @JvmOverloads constructor(
    var clientAppName: String,
    var clientAppVersion: String,
    var clientAppBuild: String,
    var apiKey: String,
    var checkRedirectScheme: Boolean = true,
    var authenticationMode: AuthenticationMode = AuthenticationMode.NATIVE,
    var environment: Environment = Environment.PRODUCTION,
    var extensions: List<String> = emptyList(),
    var domainACL: List<String> = listOf("pace.cloud"),
    var locationAccuracy: Int? = null,
    var speedThresholdInKmPerHour: Int = 50,
    var geoAppsScope: String = "pace-min",
    var appsDistanceThresholdInMeters: Int = 150,
    var oidConfiguration: OIDConfiguration?
)

enum class AuthenticationMode(val value: String) {
    NATIVE("Native"),
    WEB("Web")
}

enum class Theme {
    LIGHT,
    DARK
}
