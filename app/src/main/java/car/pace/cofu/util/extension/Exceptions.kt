package car.pace.cofu.util.extension

import androidx.annotation.StringRes
import car.pace.cofu.R
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.poikit.utils.ApiException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UserCanceledException : Exception("Flow canceled by the user")
class MailNotSentException : Exception("Mail could not be sent")

@StringRes
fun Throwable.errorTextRes(): Int {
    return when (this) {
        is InternalError -> R.string.onboarding_error_authorization
        is UnknownHostException, is SocketTimeoutException -> R.string.common_use_network_error
        is ApiException -> if (errorCode == HttpURLConnection.HTTP_FORBIDDEN) R.string.onboarding_error_authorization else R.string.common_use_network_error
        else -> R.string.common_use_unknown_error
    }
}
