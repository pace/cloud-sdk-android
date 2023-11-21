package car.pace.cofu.util.extension

import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.resumeIfActive
import kotlinx.coroutines.CancellableContinuation

/**
 * Resumes the [CancellableContinuation] with [Result.success] if the [completion] is [Success]
 * or with the [Result.failure] if the [completion] is [Failure].
 */
fun <T> CancellableContinuation<Result<T>>.resume(completion: Completion<T>) {
    when (completion) {
        is Success -> resumeIfActive(Result.success(completion.result))
        is Failure -> resumeIfActive(Result.failure(completion.throwable))
    }
}
