package cloud.pace.sdk.utils

enum class Environment(
    val idUrl: String,
    val apiUrl: String,
    val searchBaseUrl: String,
    val routingBaseUrl: String
) {

    DEVELOPMENT(
        "https://id.dev.pace.cloud",
        "https://api.dev.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/"
    ),
    STAGING(
        "https://id.stage.pace.cloud",
        "https://api.stage.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/"
    ),
    PRODUCTION(
        "https://id.pace.cloud",
        "https://api.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/"
    ),
    SANDBOX(
        "https://id.sandbox.pace.cloud",
        "https://api.sandbox.pace.cloud",
        "https://api.pace.cloud/photon/",
        "https://maps.pacelink.net/osrm5/route/v1/"
    )
}
