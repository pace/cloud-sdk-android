package cloud.pace.sdk.appkit.utils

import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

object TokenValidator {

    private const val TEN_MINUTES_IN_SECONDS = 60 * 10

    /**
     * Checks if the passed [accessToken] is not expired.
     *
     * @return True if the access token is valid, false otherwise
     */
    fun isTokenValid(accessToken: String): Boolean {
        val decodedJwtPayload: JSONObject?
        val expirationTime: Long
        try {
            decodedJwtPayload = JWTUtils.decodeJwtPayload(accessToken)
            expirationTime = decodedJwtPayload.getLong("exp")
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode token $accessToken or its expiration date")
            return false
        }

        return expirationTime > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - TEN_MINUTES_IN_SECONDS
    }

    /**
     * Returns the scopes of the passed [accessToken].
     *
     * @return A list of scopes
     */
    fun scopes(accessToken: String): List<String> {
        val decodedJwtPayload: JSONObject?
        return try {
            decodedJwtPayload = JWTUtils.decodeJwtPayload(accessToken)
            decodedJwtPayload.getString("scope").split(" ")
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode token $accessToken or its scopes")
            return emptyList()
        }
    }

    /**
     * Returns a list of payment method kinds that are currently allowed to be onboarded for the passed [accessToken].
     *
     * @return A list of payment method kinds e.g. `paypal`, `paydirekt`, `creditcard`
     */
    fun paymentMethodKinds(accessToken: String): List<String> {
        val scopes = scopes(accessToken)
        val prefix = "payment-method:create:"

        return scopes.mapNotNull {
            if (it.startsWith(prefix)) {
                it.substring(prefix.length)
            } else {
                null
            }
        }
    }
}
