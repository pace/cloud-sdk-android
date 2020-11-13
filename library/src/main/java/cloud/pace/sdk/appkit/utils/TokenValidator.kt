package cloud.pace.sdk.appkit.utils

import cloud.pace.sdk.utils.Log
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TokenValidator {

    private const val TEN_MINUTES_IN_SECONDS = 60 * 10

    fun isTokenValid(token: String): Boolean {
        val decodedJwtPayload: JSONObject?
        val expirationTime: Long
        try {
            decodedJwtPayload = JWTUtils.decodeJwtPayload(token)
            expirationTime = decodedJwtPayload.getLong("exp")
        } catch (e: Exception) {
            Log.e("TokenValidator: Failed to decode token $token or its expiration date - Error: ${e.message}")
            return false
        }

        return expirationTime > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - TEN_MINUTES_IN_SECONDS
    }
}
