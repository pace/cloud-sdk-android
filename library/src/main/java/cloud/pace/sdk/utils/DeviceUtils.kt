package cloud.pace.sdk.utils

import android.os.Build
import okio.Buffer

/**
 * Utils for device attributes.
 *
 * Most of this is a copy from PACEKitConfig.
 */
object DeviceUtils {

    /**
     * Returns the release version
     */
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    /**
     * Returns the device name formatted the following way:
     * `[BRAND or MANUFACTURER] MODEL ([PRODUCT or DEVICE])`
     *
     * Example output:
     *
     * - OnePlus ONEPLUS A3003 (OnePlus3)
     * - samsung SM-G935F (hero2ltexx)
     * - Sony D6603 (D6603)
     * - google Nexus 5X (bullhead)
     * - Huawei ALE-L21 (ALE-L21)
     *
     * @see getDeviceModel
     * @see getDeviceModelVersion
     *
     * @see Build.BRAND
     * @see Build.MANUFACTURER
     * @see Build.MODEL
     * @see Build.PRODUCT
     * @see Build.DEVICE
     */
    fun getDeviceName(): String {
        return "${getDeviceModel()} (${getDeviceModelVersion()})"
    }

    /**
     * Returns the device model version formatted the following way:
     * `PRODUCT or DEVICE`
    q
     * Example output:
     *
     * - OnePlus3
     * - hero2ltexx
     * - D6603
     * - bullhead
     * - ALE-L21
     *
     * @see Build.PRODUCT
     * @see Build.DEVICE
     */
    fun getDeviceModelVersion(): String {
        return if (!Build.PRODUCT.isNullOrEmpty()) filterString(Build.PRODUCT) else filterString(Build.DEVICE)
    }

    /**
     * Returns the device model formatted the following way:
     * `[BRAND or MANUFACTURER] MODEL`
     *
     * Example output:
     *
     * - OnePlus ONEPLUS A3003
     * - samsung SM-G935F
     * - Sony D6603
     * - google Nexus 5X
     * - Huawei ALE-L21
     *
     * @see Build.BRAND
     * @see Build.MANUFACTURER
     * @see Build.MODEL
     */
    fun getDeviceModel(): String {
        val manufacturer = if (!Build.BRAND.isNullOrEmpty()) Build.BRAND else Build.MANUFACTURER
        return filterString("$manufacturer ${Build.MODEL}")
    }

    /**
     * Replaces control characters and non-ASCII characters of the string with "?" and returns it.
     * Used to avoid an [IllegalArgumentException] in the OkHttp header.
     * @param string Input string for filtering
     * @return Filtered [string]
     */
    private fun filterString(string: String): String {
        var i = 0
        while (i < string.length) {
            var codePoint = string.codePointAt(i)
            if ((codePoint <= '\u001f'.toInt() && codePoint != '\t'.toInt()) || codePoint >= '\u007f'.toInt()) {
                val buffer = Buffer()
                buffer.writeUtf8(string, 0, i)

                var j = i
                while (j < string.length) {
                    codePoint = string.codePointAt(j)
                    buffer.writeUtf8CodePoint(if (codePoint > '\u001f'.toInt() && codePoint < '\u007f'.toInt()) codePoint else '?'.toInt())
                    j += Character.charCount(codePoint)
                }
                return buffer.readUtf8()
            }

            i += Character.charCount(codePoint)
        }
        return string
    }
}
