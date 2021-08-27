package car.pace.cofu.core.events

import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A special implementation of [MutableLiveData] that allows queuing of objects. If the
 * corresponding [LifecycleOwner] is not active, the objects are stored in a [Queue] and dispatched
 * once the [LifecycleOwner] is active again.
 */
class QueueLiveEvent<T> : MutableLiveData<T>() {

    private var active = false

    private val queue: Queue<T> = ArrayDeque(128)

    private val mPending = AtomicBoolean(false)

    @MainThread
    fun observe(owner: LifecycleOwner, onChanged: (t: T?) -> Unit) {
        if (hasActiveObservers()) {
            Timber.w("Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner, Observer<T> { t ->
            if (mPending.compareAndSet(true, false)) {
                onChanged(t)
            }
        })
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Timber.w("Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner, Observer { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(value: T?) {
        mPending.set(true)
        super.setValue(value)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

    @CallSuper
    override fun onActive() {
        super.onActive()
        active = true
        while (queue.isNotEmpty()) {
            value = queue.remove()
        }
    }

    @CallSuper
    override fun onInactive() {
        super.onInactive()
        active = false
    }

    /**
     * Sets the value to the given one and dispatches it.
     *
     * In contrast to [postValue], this method guarantees that the value is dispatched if the
     * [LifecycleOwner] is currently active, even if multiple calls to this method are made in a
     * short period of time.
     */
    fun dispatchValue(newValue: T?) {
        uiThread { value = newValue }
    }

    /**
     * Sets the value to the given one and dispatches it,
     * if this cannot be done immediately, it is queued.
     *
     * In contrast to [postValue], this method guarantees that the value is dispatched if the
     * [LifecycleOwner] is currently active, even if multiple calls to this method are made in a
     * short period of time. If the corresponding observer is not active, it is queued.
     */
    fun queueValue(newValue: T?) {
        uiThread {
            if (active) {
                value = newValue
            } else {
                queue.add(newValue)
            }
        }
    }

    /**
     * Delegation to the UI-thread (if not in it)
     */
    private fun uiThread(f: () -> Unit) {
        if (ContextHelper.mainThread == Thread.currentThread()) {
            f()
        } else {
            ContextHelper.handler.post { f() }
        }
    }

    object ContextHelper {
        val handler = Handler(Looper.getMainLooper())
        val mainThread: Thread = Looper.getMainLooper().thread
    }

}

