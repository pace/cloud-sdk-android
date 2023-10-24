package car.pace.cofu.util

/**
 * A generic class that holds a value with its loading status.
 */
sealed class UiState<out R> {

    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val throwable: Throwable) : UiState<Nothing>()
    object Loading : UiState<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[throwable=$throwable]"
            Loading -> "Loading"
        }
    }
}

val <T> UiState<T>.data: T?
    get() = (this as? UiState.Success)?.data
