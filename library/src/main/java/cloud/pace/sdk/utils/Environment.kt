package cloud.pace.sdk.utils

enum class Environment(
    val idUrl: String,
    val apiUrl: String,
    val searchBaseUrl: String,
    val routingBaseUrl: String,
    val payUrl: String,
    val transactionUrl: String
) {

    DEVELOPMENT(
        "https://id.dev.pace.cloud",
        "https://api.dev.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/",
        "https://pay.dev.pace.cloud",
        "https://pay.dev.pace.cloud/transactions"
    ),
    STAGING(
        "https://id.stage.pace.cloud",
        "https://api.stage.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/",
        "https://pay.stage.pace.cloud",
        "https://pay.stage.pace.cloud/transactions"
    ),
    PRODUCTION(
        "https://id.pace.cloud",
        "https://api.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/",
        "https://pay.pace.cloud",
        "https://pay.pace.cloud/transactions"
    ),
    SANDBOX(
        "https://id.sandbox.pace.cloud",
        "https://api.sandbox.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/",
        "https://pay.sandbox.pace.cloud",
        "https://pay.sandbox.pace.cloud/transactions"
    )
}
