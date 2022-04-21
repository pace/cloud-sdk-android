package cloud.pace.sdk.poikit.utils

class ApiException @JvmOverloads constructor(var errorCode: Int, var errorMessage: String, var requestId: String? = null) : Exception() {
    override fun toString(): String {
        return super.toString() + "\n" +
            "code = $errorCode || message = $errorMessage || request ID = $requestId"
    }
}
