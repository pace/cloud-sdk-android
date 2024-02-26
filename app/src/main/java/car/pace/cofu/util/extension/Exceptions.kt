package car.pace.cofu.util.extension

import androidx.annotation.StringRes
import car.pace.cofu.R
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.idkit.model.PINNotSecure
import cloud.pace.sdk.poikit.utils.ApiException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LocationDisabledException : Throwable("Location services disabled")
class LocationPermissionDeniedException : Throwable("Location permission denied")
class MailNotSentException : Exception("Mail could not be sent")
class InvalidLengthException : Exception("PIN has not the correct length of 4 digits")
class TooFewDigitsException : Exception("PIN does not contain at least 3 different digits")
class SeriesNotAllowedException : Exception("PIN contains a series of ascending or descending digits")
class PinMismatchException : Exception("PIN input mismatches")

@StringRes
fun Throwable.errorTextRes(): Int {
    return when (this) {
        is LocationDisabledException -> R.string.LOCATION_DIALOG_DISABLED_TITLE
        is LocationPermissionDeniedException -> R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE
        is InternalError -> R.string.onboarding_error_authorization
        is UnknownHostException, is SocketTimeoutException -> R.string.common_use_network_error
        is ApiException -> if (errorCode == HttpURLConnection.HTTP_FORBIDDEN) R.string.onboarding_error_authorization else R.string.common_use_network_error
        is InvalidLengthException -> R.string.onboarding_pin_error_invalid_length
        is TooFewDigitsException -> R.string.onboarding_pin_error_too_few_digits
        is SeriesNotAllowedException -> R.string.onboarding_pin_error_series
        is PinMismatchException -> R.string.onboarding_pin_error_mismatch
        is PINNotSecure -> R.string.onboarding_pin_error_not_secure
        else -> R.string.common_use_unknown_error
    }
}
