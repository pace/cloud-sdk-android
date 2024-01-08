package car.pace.cofu.config

const val CONFIG_FILE_NAME = "config.json"

data class Config(
    val appName: String,
    val signing: Signing,
    val sdk: Sdk,
    val sentry: Sentry,
    val crashlyticsEnabled: Boolean,
    val hidePrices: Boolean,
    val googleMapsApiKey: String,
    val onboardingShowCustomHeader: Boolean,
    val homeShowCustomHeader: Boolean,
    val menuEntries: List<List<MenuEntry>>
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

data class MenuEntry(
    val languageCode: String,
    val name: String,
    val url: String
)
