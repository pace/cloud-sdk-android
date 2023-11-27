package car.pace.cofu.util

import cloud.pace.sdk.appkit.utils.JWTUtils
import org.json.JSONObject
import timber.log.Timber

object JWTUtils {

    /**
     * Decodes access token and returns user email
     */
    fun getUserEMailFromToken(accessToken: String?): String? {
        accessToken ?: return null

        val decodedJwtPayload: JSONObject?
        val mail: String
        try {
            decodedJwtPayload = JWTUtils.decodeJwtPayload(accessToken)
            mail = decodedJwtPayload.getString("email")
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode token $accessToken or its user email")
            return null
        }

        return mail
    }
}
