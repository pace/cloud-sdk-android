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
     * If it returns `null`, no payment method kinds are allowed to be onboarded.
     * If it returns an empty list, all payment method kinds are allowed to be onboarded.
     *
     * @return A list of payment method kinds (e.g. `paypal`, `paydirekt`, `creditcard`), `null` or an empty list
     */
    fun paymentMethodKinds(accessToken: String): List<String>? {
        val scopes = scopes(accessToken)
        val paymentMethodCreateScope = "pay:payment-methods:create"
        val prefix = "$paymentMethodCreateScope:"

        if (scopes.contains(paymentMethodCreateScope)) {
            // The wildcard scope is included which means that all payment method kinds are supported
            return emptyList()
        }

        val kinds = scopes.mapNotNull {
            if (it.startsWith(prefix)) {
                it.substring(prefix.length)
            } else {
                null
            }
        }

        // Return null to indicate that no payment method kinds are supported
        return if (kinds.isEmpty()) null else kinds
    }
}
