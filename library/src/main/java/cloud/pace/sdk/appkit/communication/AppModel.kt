package cloud.pace.sdk.appkit.communication

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.appkit.utils.TokenValidator
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.utils.*
import net.openid.appauth.AuthorizationException
import timber.log.Timber

interface AppModel {

    var callback: AppCallbackImpl?
    val close: LiveData<Unit>
    val openUrlInNewTab: LiveData<String>
    val authorize: LiveData<Event<Result<Completion<String?>>>>
    val endSession: LiveData<Event<Result<LogoutResponse>>>

    fun reset()
    fun authorize(onResult: (Completion<String?>) -> Unit)
    fun endSession(onResult: (LogoutResponse) -> Unit)
    fun close()
    fun openUrlInNewTab(url: String)
    fun disable(host: String)
    fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (Completion<GetAccessTokenResponse>) -> Unit)
    fun onLogin(context: Context, result: Completion<String?>)
    fun onLogout(onResult: (LogoutResponse) -> Unit)
    fun onCustomSchemeError(context: Context?, scheme: String)
    fun onImageDataReceived(bitmap: Bitmap)
    fun setUserProperty(key: String, value: String, update: Boolean)
    fun logEvent(key: String, parameters: Map<String, Any>)
    fun getConfig(key: String, config: (String?) -> Unit)

    class Result<T>(val onResult: (T) -> Unit)
}

class AppModelImpl : AppModel {

    override var callback: AppCallbackImpl? = null
    override var close = MutableLiveData<Unit>()
    override var openUrlInNewTab = MutableLiveData<String>()
    override val authorize = MutableLiveData<Event<AppModel.Result<Completion<String?>>>>()
    override val endSession = MutableLiveData<Event<AppModel.Result<LogoutResponse>>>()

    override fun reset() {
        close = MutableLiveData()
        openUrlInNewTab = MutableLiveData()
    }

    override fun authorize(onResult: (Completion<String?>) -> Unit) {
        onMainThread {
            authorize.value = Event(AppModel.Result(onResult))
        }
    }

    override fun endSession(onResult: (LogoutResponse) -> Unit) {
        onMainThread {
            endSession.value = Event(AppModel.Result(onResult))
        }
    }

    override fun close() {
        onMainThread {
            close.value = Unit
            callback?.onClose()
        }
    }

    override fun openUrlInNewTab(url: String) {
        onMainThread {
            openUrlInNewTab.value = url
            callback?.onOpenInNewTab(url)
        }
    }

    override fun disable(host: String) {
        onMainThread {
            callback?.onDisable(host)
        }
    }

    override fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (Completion<GetAccessTokenResponse>) -> Unit) {
        if (IDKit.isInitialized) {
            if (reason == InvalidTokenReason.UNAUTHORIZED && oldToken != null && TokenValidator.isTokenValid(oldToken)) {
                Timber.wtf("Reason is UNAUTHORIZED and token is still valid -> send onSessionRenewalFailed()")
                sendOnSessionRenewalFailed(null, onResult)
            } else {
                if (IDKit.isAuthorizationValid()) {
                    IDKit.refreshToken { completion ->
                        when (completion) {
                            is Success -> completion.result?.let { token -> onResult(Success(GetAccessTokenResponse(token))) } ?: onResult(Failure(InternalError))
                            is Failure -> {
                                val code = (completion.throwable as? AuthorizationException)?.code
                                if (code != null && code >= 1000) {
                                    /**
                                     * Keycloak error --> For a full list of codes see [net.openid.appauth.AuthorizationException]
                                     */
                                    Timber.wtf("Keycloak error -> send onSessionRenewalFailed()")
                                    sendOnSessionRenewalFailed(completion.throwable, onResult)
                                } else {
                                    // General error (e.g. network error)
                                    onResult(Failure(InternalError))
                                }
                            }
                        }
                    }
                } else {
                    authorize { completion ->
                        (completion as? Success)?.result?.let {
                            onResult(Success(GetAccessTokenResponse(it, true)))
                        } ?: onResult(Failure(InternalError))
                    }
                }
            }
        } else {
            onMainThread {
                callback?.getAccessToken(reason, oldToken) {
                    onResult(Success(it))
                }
            }
        }
    }

    private fun sendOnSessionRenewalFailed(throwable: Throwable?, onResult: (Completion<GetAccessTokenResponse>) -> Unit) {
        onMainThread {
            callback?.onSessionRenewalFailed(throwable) {
                it?.let { onResult(Success(GetAccessTokenResponse(it))) } ?: onResult(Failure(InternalError))
            }
        }
    }

    override fun onLogin(context: Context, result: Completion<String?>) {
        onMainThread {
            callback?.onLogin(context, result)
        }
    }

    override fun onLogout(onResult: (LogoutResponse) -> Unit) {
        onMainThread {
            callback?.onLogout(onResult)
        }
    }

    override fun onCustomSchemeError(context: Context?, scheme: String) {
        onMainThread {
            callback?.onCustomSchemeError(context, scheme)
        }
    }

    override fun onImageDataReceived(bitmap: Bitmap) {
        onMainThread {
            callback?.onImageDataReceived(bitmap)
        }
    }

    override fun setUserProperty(key: String, value: String, update: Boolean) {
        onMainThread {
            callback?.setUserProperty(key, value, update)
        }
    }

    override fun logEvent(key: String, parameters: Map<String, Any>) {
        onMainThread {
            callback?.logEvent(key, parameters)
        }
    }

    override fun getConfig(key: String, config: (String?) -> Unit) {
        onMainThread {
            callback?.getConfig(key, config)
        }
    }
}
