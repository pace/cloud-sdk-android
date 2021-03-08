package cloud.pace.sdk.poikit.utils

class ApiException(var errorCode: Int, var errorMessage: String) : Exception() {
    override fun toString(): String {
        return super.toString() + "\n" +
            "code = $errorCode || message = $errorMessage"
    }
}
