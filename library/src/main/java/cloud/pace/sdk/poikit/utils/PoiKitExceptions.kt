package cloud.pace.sdk.poikit.utils

open class PoiKitExceptions : Exception()
class ZoomException : PoiKitExceptions()
class ApiException(val errorCode: Int, val errorMessage: String) : Exception() {
    override fun toString(): String {
        return super.toString() + "\n" +
            "code = $errorCode || message = $errorMessage"
    }
}
