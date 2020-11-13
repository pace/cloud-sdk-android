package cloud.pace.sdk.utils

import java.util.concurrent.*

/**
 * A backport of Java `CompletableFuture` which works with old Androids.
 */
class CompletableFutureCompat<V> : Future<V> {
    private sealed class Result<out V> {
        abstract val value: V

        class Ok<V>(override val value: V) : Result<V>()
        class Error(val e: Throwable) : Result<Nothing>() {
            override val value: Nothing
                get() = throw e
        }

        object Cancel : Result<Nothing>() {
            override val value: Nothing
                get() = throw CancellationException()
        }
    }

    /**
     * Offers the completion result for [result].
     *
     * If this queue is not empty, the future is completed.
     */
    private val completion = LinkedBlockingQueue<Result<V>>(1)

    /**
     * Holds the result of the computation. Takes the item from [completion] upon running and provides it as a result.
     */
    private val result = FutureTask<V> { completion.peek()!!.value }

    /**
     * If not already completed, causes invocations of [get]
     * and related methods to throw the given exception.
     *
     * @param ex the exception
     * @return `true` if this invocation caused this CompletableFuture
     * to transition to a completed state, else `false`
     */
    fun completeExceptionally(ex: Throwable): Boolean {
        val offered = completion.offer(Result.Error(ex))
        if (offered) {
            result.run()
        }
        return offered
    }

    /**
     * If not already completed, completes this CompletableFuture with
     * a [CancellationException].
     *
     * @param mayInterruptIfRunning this value has no effect in this
     * implementation because interrupts are not used to control
     * processing.
     *
     * @return `true` if this task is now cancelled
     */
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        val offered = completion.offer(Result.Cancel)
        if (offered) {
            result.cancel(mayInterruptIfRunning)
        }
        return offered
    }

    /**
     * If not already completed, sets the value returned by [get] and related methods to the given value.
     *
     * @param value the result value
     * @return `true` if this invocation caused this CompletableFuture
     * to transition to a completed state, else `false`
     */
    fun complete(value: V): Boolean {
        val offered = completion.offer(Result.Ok(value))
        if (offered) {
            result.run()
        }
        return offered
    }

    override fun isDone(): Boolean = completion.isNotEmpty()

    override fun get(): V = result.get()

    override fun get(timeout: Long, unit: TimeUnit): V = result.get(timeout, unit)

    override fun isCancelled(): Boolean = completion.peek() == Result.Cancel
}
