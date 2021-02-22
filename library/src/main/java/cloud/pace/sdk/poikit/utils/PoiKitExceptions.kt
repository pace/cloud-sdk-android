package cloud.pace.sdk.poikit.utils

class ApiException(private val errorCode: Int, private val errorMessage: String) : Exception() {
    override fun toString(): String {
        return super.toString() + "\n" +
            "code = $errorCode || message = $errorMessage"
    }
}
