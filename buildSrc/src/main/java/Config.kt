data class Config(
    val appName: String,
    val signing: Signing,
    val sdk: Sdk
)

data class Signing(
    val keyPath: String,
    val keyPassword: String,
    val keyAlias: String,
    val keyAliasPassword: String
)

data class Sdk(
    val apiKey: String,
    val clientId: String,
    val redirectUrl: String,
    val redirectScheme: String,
    val uniqueId: String
)
