package cloud.pace.sdk.idkit

import android.net.Uri

object InvalidSession : Exception("The current session is either invalid or expired which may be caused by resetting the session beforehand. Authorize again to create a new session.")
object FailedRetrievingSessionWhileAuthorizing : Exception("The authorization request failed because the session couldn't be retrieved.")
object FailedRetrievingConfigurationWhileDiscovering : Exception("The discovery failed because the configuration couldn't be retrieved.")
object FailedRetrievingSessionWhileEnding : Exception("The end session request failed because the session couldn't be retrieved.")
object UserEndpointNotDefined : Exception("The user endpoint was not defined.")
data class AuthorizationError(
    val type: Int,
    val code: Int,
    val error: String? = null,
    val errorDescription: String? = null,
    val errorUri: Uri? = null,
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)
