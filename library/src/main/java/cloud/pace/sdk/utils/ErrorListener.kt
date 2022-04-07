package cloud.pace.sdk.utils

interface ErrorListener {

    fun reportError(exception: Exception)
    fun reportBreadcrumb(category: String, message: String, data: Map<String, Any?>? = null, level: ErrorLevel? = null)

    companion object {

        internal var errorListener: ErrorListener? = null

        internal fun reportError(exception: Exception) {
            errorListener?.reportError(exception)
        }

        internal fun reportBreadcrumb(category: String, message: String, data: Map<String, Any?>? = null, level: ErrorLevel? = null) {
            errorListener?.reportBreadcrumb(category, message, data, level)
        }
    }
}

enum class ErrorLevel {
    DEBUG, INFO, WARNING, ERROR, FATAL
}
