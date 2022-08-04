package cloud.pace.sdk.utils

enum class Environment(
    @Deprecated("Use URL.paceID instead", ReplaceWith("URL.paceID"))
    val idUrl: String,
    val apiUrl: String,
    val searchBaseUrl: String,
    val routingBaseUrl: String,
    @Deprecated("Use URL.payment instead", ReplaceWith("URL.payment"))
    val payUrl: String,
    @Deprecated("Use URL.transactions instead", ReplaceWith("URL.transactions"))
    val transactionUrl: String,
    @Deprecated("Use URL.fueling instead", ReplaceWith("URL.fueling"))
    val fuelingUrl: String,
    @Deprecated("Use URL.dashboard instead", ReplaceWith("URL.dashboard"))
    val dashboardUrl: String,
    val cdnUrl: String
) {

    DEVELOPMENT(
        "https://id.dev.pace.cloud",
        "https://api.dev.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.dev.pace.cloud",
        "https://pay.dev.pace.cloud/transactions",
        "https://dev.fuel.site",
        "https://my.dev.fuel.site",
        "https://cdn.dev.pace.cloud"
    ),
    PRODUCTION(
        "https://id.pace.cloud",
        "https://api.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.pace.cloud",
        "https://pay.pace.cloud/transactions",
        "https://fuel.site",
        "https://my.fuel.site",
        "https://cdn.pace.cloud"
    ),
    SANDBOX(
        "https://id.sandbox.pace.cloud",
        "https://api.sandbox.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.sandbox.pace.cloud",
        "https://pay.sandbox.pace.cloud/transactions",
        "https://sandbox.fuel.site",
        "https://my.sandbox.fuel.site",
        "https://cdn.sandbox.pace.cloud"
    )
}
