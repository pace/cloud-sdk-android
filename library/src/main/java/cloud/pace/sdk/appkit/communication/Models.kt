package cloud.pace.sdk.appkit.communication

import cloud.pace.sdk.appkit.communication.InvalidTokenReason.OTHER
import cloud.pace.sdk.appkit.communication.InvalidTokenReason.UNAUTHORIZED
import java.net.HttpURLConnection

/**
 * Specifies the reason why the access token is invalid.
 * [UNAUTHORIZED] means that the session has been invalidated and [OTHER] means that the access token has expired and should be renewed.
 *
 * @see AppCallback.getAccessToken
 */
enum class InvalidTokenReason(val value: String) {
    UNAUTHORIZED("unauthorized"),
    OTHER("other")
}

/**
 * The response class of an [AppCallback.getAccessToken] request.
 *
 * @param accessToken The new access token (after authorization or token refresh)
 * @param isInitialToken Set to `true` if the user has logged in (no previous session before) or to `false` if the access token was refreshed for an existing session. Defaults to `false`.
 *
 * @see AppCallback.getAccessToken
 */
data class GetAccessTokenResponse @JvmOverloads constructor(val accessToken: String, val isInitialToken: Boolean = false)

/**
 * Specifies the response type of an [AppCallback.onLogout] request.
 * Use [LogoutResponse.SUCCESSFUL] to signal a successful logout, [LogoutResponse.UNAUTHORIZED] if the user was not logged in at all and [LogoutResponse.OTHER] for all other errors.
 *
 * @param statusCode The status code of the logout response.
 *
 * @see AppCallback.onLogout
 */
enum class LogoutResponse(val statusCode: Int) {
    SUCCESSFUL(HttpURLConnection.HTTP_NO_CONTENT),
    UNAUTHORIZED(HttpURLConnection.HTTP_NOT_FOUND),
    OTHER(HttpURLConnection.HTTP_INTERNAL_ERROR)
}
