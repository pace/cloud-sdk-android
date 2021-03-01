package cloud.pace.sdk.idkit

import android.net.Uri

data class ServiceConfiguration(
    val authorizationEndpoint: Uri,
    val tokenEndpoint: Uri,
    val endSessionEndpoint: Uri?,
    val registrationEndpoint: Uri?
)
