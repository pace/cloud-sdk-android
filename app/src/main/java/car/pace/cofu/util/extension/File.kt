package car.pace.cofu.util.extension

import android.content.Context
import car.pace.cofu.util.LogAndBreadcrumb
import java.io.InputStream
import java.security.MessageDigest

/**
 * **Note**: It is the caller's responsibility to close this stream.
 */
fun Context.openAsset(fileNameWithExtension: String): InputStream? {
    return try {
        assets.open(fileNameWithExtension)
    } catch (e: Exception) {
        LogAndBreadcrumb.e(e, LogAndBreadcrumb.FILE, "Could not open asset file $fileNameWithExtension")
        null
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun InputStream.hash(algorithm: String = "MD5"): String? {
    return try {
        val messageDigest = MessageDigest.getInstance(algorithm)
        val digest = messageDigest.digest(readBytes())
        digest.toHexString()
    } catch (e: Exception) {
        LogAndBreadcrumb.e(e, LogAndBreadcrumb.FILE, "Could not hash data with $algorithm algorithm")
        null
    }
}
