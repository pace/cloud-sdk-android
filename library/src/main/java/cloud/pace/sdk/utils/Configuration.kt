package cloud.pace.sdk.utils

data class Configuration @JvmOverloads constructor(
    var clientAppName: String,
    var clientAppVersion: String,
    var clientAppBuild: String,
    var apiKey: String,
    var checkRedirectScheme: Boolean = true,
    var authenticationMode: AuthenticationMode = AuthenticationMode.NATIVE,
    var environment: Environment = Environment.PRODUCTION,
    var extensions: List<String> = emptyList(),
    var domainACL: List<String> = emptyList(),
    var locationAccuracy: Int? = null,
    var speedThresholdInKmPerHour: Int = 50,
    var geoAppsScope: String = "pace",
    var appsDistanceThresholdInMeters: Int = 150
)

enum class AuthenticationMode(val value: String) {
    NATIVE("Native"),
    WEB("Web")
}

enum class Theme {
    LIGHT,
    DARK
}
