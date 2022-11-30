package cloud.pace.sdk.appkit.utils

import android.util.Base64
import org.json.JSONObject
import timber.log.Timber

object JWTUtils {

    fun decodeJwtPayload(token: String): JSONObject {
        val parts = token.split(".").toTypedArray()
        return JSONObject(String(Base64.decode(parts[1], Base64.DEFAULT)))
    }

    /**
     * Decodes access token and returns user id
     */
    fun getUserIDFromToken(accessToken: String): String? {
        val decodedJwtPayload: JSONObject?
        val userID: String
        try {
            decodedJwtPayload = JWTUtils.decodeJwtPayload(accessToken)
            userID = decodedJwtPayload.getString("sub")
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode token $accessToken or its user ID")
            return null
        }

        return userID
    }
}
