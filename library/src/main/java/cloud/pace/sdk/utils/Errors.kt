package cloud.pace.sdk.utils

import cloud.pace.sdk.PACECloudSDK

object PermissionDenied : Exception("The location permission is not granted.")
object NetworkError : Exception("Could not connect to the network.")
object NoLocationFound : Exception("Could not find a location via GPS or network.")
object InvalidSpeed : Exception("The speed is greater than or equal to the 'speedThresholdInKmPerHour' of ${PACECloudSDK.configuration.speedThresholdInKmPerHour} km/h.")
