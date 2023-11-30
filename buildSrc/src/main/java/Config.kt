data class Config(
    val appName: String,
    val signing: Signing,
    val sdk: Sdk,
    val sentry: Sentry,
    val crashlyticsEnabled: Boolean,
    val hidePrices: Boolean,
    val googleMapsApiKey: String
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

data class Sentry(
    val enabled: Boolean,
    val dsn: String
)
