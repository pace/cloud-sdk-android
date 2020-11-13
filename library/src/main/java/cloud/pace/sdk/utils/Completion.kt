package cloud.pace.sdk.utils

sealed class Completion<T>
class Success<T>(val result: T) : Completion<T>()
class Failure<T>(val throwable: Throwable) : Completion<T>()
