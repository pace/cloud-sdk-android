package cloud.pace.sdk.appkit.communication

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface AppModel {

    var callback: AppCallbackImpl?
    val close: LiveData<Unit>
    val openUrlInNewTab: LiveData<String>
    val disable: LiveData<String>

    fun reset()
    fun close()
    fun openUrlInNewTab(url: String)
    fun disable(host: String)
    fun onTokenInvalid(onResult: (String) -> Unit)
    fun onCustomSchemeError(context: Context?, scheme: String)
    fun onImageDataReceived(bitmap: Bitmap)
}

class AppModelImpl : AppModel {

    override var callback: AppCallbackImpl? = null
    override var close = MutableLiveData<Unit>()
    override var openUrlInNewTab = MutableLiveData<String>()
    override var disable = MutableLiveData<String>()

    override fun reset() {
        close = MutableLiveData()
        openUrlInNewTab = MutableLiveData()
        disable = MutableLiveData()
    }

    override fun close() {
        close.value = Unit
        callback?.onClose()
    }

    override fun openUrlInNewTab(url: String) {
        openUrlInNewTab.value = url
        callback?.onOpenInNewTab(url)
    }

    override fun disable(host: String) {
        disable.value = host
        callback?.onDisable(host)
    }

    override fun onTokenInvalid(onResult: (String) -> Unit) {
        callback?.onTokenInvalid(onResult)
    }

    override fun onCustomSchemeError(context: Context?, scheme: String) {
        callback?.onCustomSchemeError(context, scheme)
    }

    override fun onImageDataReceived(bitmap: Bitmap) {
        callback?.onImageDataReceived(bitmap)
    }
}
