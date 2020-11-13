package cloud.pace.sdk.appkit.utils

object PermissionDenied : Exception("The location permission is not granted.")
object NetworkError : Exception("Could not connect to the network.")
object NoLocationFound : Exception("Could not find a location via GPS or network.")
object RunningCheck : Exception("A check for apps is already running.")
