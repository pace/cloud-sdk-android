package cloud.pace.sdk.appkit.communication

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.appkit.model.InvalidTokenReason
import cloud.pace.sdk.utils.onMainThread

interface AppModel {

    var callback: AppCallbackImpl?
    val close: LiveData<Pair<Boolean, List<String>?>>
    val openUrlInNewTab: LiveData<String>

    fun reset()
    fun close(force: Boolean = false, urls: List<String>? = null)
    fun openUrlInNewTab(url: String)
    fun disable(host: String)
    fun onTokenInvalid(reason: InvalidTokenReason, oldToken: String?, onResult: (String) -> Unit)
    fun onCustomSchemeError(context: Context?, scheme: String)
    fun onImageDataReceived(bitmap: Bitmap)
    fun setUserProperty(key: String, value: String, update: Boolean)
    fun logEvent(key: String, parameters: Map<String, Any>)
    fun getConfig(key: String, config: (String?) -> Unit)
}

class AppModelImpl : AppModel {

    override var callback: AppCallbackImpl? = null
    override var close = MutableLiveData<Pair<Boolean, List<String>?>>()
    override var openUrlInNewTab = MutableLiveData<String>()

    override fun reset() {
        close = MutableLiveData()
        openUrlInNewTab = MutableLiveData()
    }

    override fun close(force: Boolean, urls: List<String>?) {
        onMainThread {
            close.value = force to urls
        }
        callback?.onClose()
    }

    override fun openUrlInNewTab(url: String) {
        onMainThread {
            openUrlInNewTab.value = url
        }
        callback?.onOpenInNewTab(url)
    }

    override fun disable(host: String) {
        callback?.onDisable(host)
    }

    override fun onTokenInvalid(reason: InvalidTokenReason, oldToken: String?, onResult: (String) -> Unit) {
        callback?.onTokenInvalid(reason, oldToken, onResult)
    }

    override fun onCustomSchemeError(context: Context?, scheme: String) {
        callback?.onCustomSchemeError(context, scheme)
    }

    override fun onImageDataReceived(bitmap: Bitmap) {
        callback?.onImageDataReceived(bitmap)
    }

    override fun setUserProperty(key: String, value: String, update: Boolean) {
        callback?.setUserProperty(key, value, update)
    }

    override fun logEvent(key: String, parameters: Map<String, Any>) {
        callback?.logEvent(key, parameters)
    }

    override fun getConfig(key: String, config: (String?) -> Unit) {
        callback?.getConfig(key, config)
    }
}
