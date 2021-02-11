package cloud.pace.sdk.utils

data class Configuration @JvmOverloads constructor(
    var clientAppName: String,
    var clientAppVersion: String,
    var clientAppBuild: String,
    var apiKey: String,
    var clientId: String? = null,
    var authenticationMode: AuthenticationMode = AuthenticationMode.WEB,
    var environment: Environment = Environment.PRODUCTION,
    var extensions: List<String> = emptyList(),
    var locationAccuracy: Int? = null
)

enum class AuthenticationMode(val value: String) {
    NATIVE("Native"),
    WEB("Web")
}

enum class Theme {
    LIGHT,
    DARK
}
