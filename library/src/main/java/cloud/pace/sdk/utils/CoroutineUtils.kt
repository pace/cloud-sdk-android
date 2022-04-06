package cloud.pace.sdk.utils

import android.os.Looper
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [launch] a new coroutine on a background thread.
 */
inline fun onIOBackgroundThread(crossinline block: suspend () -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch {
        block()
    }
}

/**
 * [launch] a new coroutine on a background thread.
 */
inline fun onBackgroundThread(crossinline block: suspend () -> Unit): Job {
    return CoroutineScope(Dispatchers.Default).launch {
        block()
    }
}

/**
 * [launch] a new coroutine on the main thread.
 */
inline fun onMainThread(crossinline block: suspend () -> Unit): Job {
    return CoroutineScope(Dispatchers.Main).launch {
        block()
    }
}

inline fun onCsvWriterThread(crossinline block: suspend () -> Unit): Job {
    return CoroutineScope(Dispatchers.Unconfined).launch(start = CoroutineStart.UNDISPATCHED) {
        block()
    }
}

/**
 * Returns the future of an [async] coroutine.
 */
inline fun <T> asyncIOBackground(crossinline block: () -> T): Deferred<T> = CoroutineScope(Dispatchers.IO).async {
    block()
}

/**
 * Returns the future of an [async] coroutine on the Background context.
 */
inline fun <T> asyncBackground(crossinline block: () -> T): Deferred<T> = CoroutineScope(Dispatchers.Default).async {
    block()
}

/**
 * [launch] a new coroutine on the main thread without inlining.
 */
fun onMainThreadNoInline(block: suspend () -> Unit): Job {
    return CoroutineScope(Dispatchers.Main).launch {
        block()
    }
}

/**
 * [launch] a new coroutine on the main
 * thread if the [block] is not already
 * called on the main thread. In that case,
 * the code is just executed without the
 * use of a coroutine.
 */
inline fun dispatchOnMainThread(crossinline block: () -> Unit) {
    val mainThreadId: Long = Looper.getMainLooper().thread.id
    if (Thread.currentThread().id != mainThreadId) {
        CoroutineScope(Dispatchers.Main).launch {
            block()
        }
    } else {
        block()
    }
}

/**
 * Resumes the execution of the corresponding coroutine if it is still active and passes [value] as the return value of the last suspension point.
 */
fun <T> CancellableContinuation<T>.resumeIfActive(value: T) {
    if (isActive) resume(value)
}

/**
 * Resumes the execution of the corresponding coroutine if it is still active so that the [exception] is re-thrown right after the last suspension point.
 */
fun <T> CancellableContinuation<T>.resumeWithExceptionIfActive(exception: Throwable) {
    if (isActive) resumeWithException(exception)
}

/**
 * Runs a given suspending [block] of code inside a [suspendCancellableCoroutine] with a specified [timeout][timeoutMillis] and throws
 * a [TimeoutCancellationException] if the timeout was exceeded.
 */
@Throws(TimeoutCancellationException::class, CancellationException::class)
suspend inline fun <T> suspendCoroutineWithTimeout(
    timeoutMillis: Long,
    crossinline block: (CancellableContinuation<T>) -> Unit
) = withTimeout(timeoutMillis) {
    suspendCancellableCoroutine(block)
}

/**
 * Resumes the [CancellableContinuation] with the [Success.result] if the [completion] is [Success]
 * or with the [Failure.throwable] if the [completion] is [Failure] so that the exception is re-thrown right after the last suspension point.
 */
fun <T> CancellableContinuation<T>.resume(completion: Completion<T>) {
    when (completion) {
        is Success -> resumeIfActive(completion.result)
        is Failure -> resumeWithExceptionIfActive(completion.throwable)
    }
}
