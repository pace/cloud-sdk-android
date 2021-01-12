package cloud.pace.sdk.utils

import android.content.res.Resources
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * Converts integer to density-independent pixels (dp).
 */
val Int.dp: Int get() = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)).toInt()

/**
 * Converts float to density-independent pixels (dp).
 */
val Float.dp: Int get() = (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)).toInt()

/**
 * Returns the UUID of an URN or null.
 */
val String.resourceUuid: String?
    get() = split(":").lastOrNull()

/**
 * Checks if [this] matches the [Universal Resource Name](https://www.ietf.org/rfc/rfc2141.txt) (URN) format.
 *
 * @return True if [this] is valid, otherwise false.
 */
fun String.isUrn(): Boolean {
    return matches("[a-z0-9][a-z0-9-]{0,31}:[a-z0-9()+,\\-.:=@;\$_!*'%/?#]+".toRegex(RegexOption.IGNORE_CASE))
}

/**
 * Returns `true` if this nullable char sequence is not `null` or empty.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun CharSequence?.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()

/**
 * Returns `true` if this nullable char sequence is not `null` or blank.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun CharSequence?.isNotNullOrBlank(): Boolean = !isNullOrBlank()

fun Date.toIso8601(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).run {
        timeZone = TimeZone.getTimeZone("UTC")
        format(this@toIso8601)
    }
}

fun Date.toRfc3339Short(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).run {
        timeZone = TimeZone.getTimeZone("UTC")
        format(this@toRfc3339Short)
    }
}

fun String.Companion.random(length: Int, pool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')): String {
    if (length < 0) {
        throw IllegalArgumentException("Length can't be less than 0")
    }

    return (1..length)
        .map { pool[Random.nextInt(0, pool.size)] }
        .joinToString("")
}
