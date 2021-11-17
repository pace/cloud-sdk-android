package cloud.pace.sdk.utils

enum class Environment(
    val idUrl: String,
    val apiUrl: String,
    val searchBaseUrl: String,
    val routingBaseUrl: String,
    val payUrl: String,
    val transactionUrl: String,
    val fuelingUrl: String,
    val dashboardUrl: String
) {

    DEVELOPMENT(
        "https://id.dev.pace.cloud",
        "https://api.dev.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.dev.pace.cloud",
        "https://pay.dev.pace.cloud/transactions",
        "https://dev.fuel.site",
        "https://my.dev.fuel.site"
    ),
    STAGING(
        "https://id.stage.pace.cloud",
        "https://api.stage.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.stage.pace.cloud",
        "https://pay.stage.pace.cloud/transactions",
        "https://stage.fuel.site",
        "https://my.stage.fuel.site"
    ),
    PRODUCTION(
        "https://id.pace.cloud",
        "https://api.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.pace.cloud",
        "https://pay.pace.cloud/transactions",
        "https://fuel.site",
        "https://my.fuel.site"
    ),
    SANDBOX(
        "https://id.sandbox.pace.cloud",
        "https://api.sandbox.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://api.pace.cloud/routing/",
        "https://pay.sandbox.pace.cloud",
        "https://pay.sandbox.pace.cloud/transactions",
        "https://sandbox.fuel.site",
        "https://my.sandbox.fuel.site"
    )
}
