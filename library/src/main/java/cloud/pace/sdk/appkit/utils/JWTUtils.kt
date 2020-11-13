package cloud.pace.sdk.appkit.utils

import android.util.Base64
import org.json.JSONObject

object JWTUtils {

    fun decodeJwtPayload(token: String): JSONObject {
        val parts = token.split(".").toTypedArray()
        return JSONObject(String(Base64.decode(parts[1], Base64.DEFAULT)))
    }
}
