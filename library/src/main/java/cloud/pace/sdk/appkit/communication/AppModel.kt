package cloud.pace.sdk.appkit.communication

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.EXTRA_TITLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.createChooser
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.utils.TokenValidator
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.onMainThread
import net.openid.appauth.AuthorizationException
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

interface AppModel {

    var callback: AppCallbackImpl?
    val close: LiveData<Unit>
    val openUrlInNewTab: LiveData<OpenURLInNewTabRequest>
    val biometricRequest: LiveData<AppWebViewModel.BiometricRequest>
    val authorize: LiveData<Result<Completion<String?>>>
    val endSession: LiveData<Result<LogoutResponse>>

    fun reset()
    fun setBiometricRequest(request: AppWebViewModel.BiometricRequest)
    fun authorize(onResult: (Completion<String?>) -> Unit)
    fun endSession(onResult: (LogoutResponse) -> Unit)
    fun close()
    fun openUrlInNewTab(openURLInNewTabRequest: OpenURLInNewTabRequest)
    fun disable(host: String)
    fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (Completion<GetAccessTokenResponse>) -> Unit)
    fun showShareSheet(bitmap: Bitmap)
    fun showShareSheet(text: String, title: String)
    fun onLogin(context: Context, result: Completion<String?>)
    fun onLogout(onResult: (LogoutResponse) -> Unit)
    fun onCustomSchemeError(context: Context?, scheme: String)
    fun onImageDataReceived(bitmap: Bitmap)
    fun setUserProperty(key: String, value: String, update: Boolean)
    fun logEvent(key: String, parameters: Map<String, Any>)
    fun getConfig(key: String, config: (String?) -> Unit)
    fun isAppRedirectAllowed(app: String, isAllowed: (Boolean) -> Unit)
    fun isSignedIn(isSignedIn: (Boolean) -> Unit)
    fun isRemoteConfigAvailable(isAvailable: (Boolean) -> Unit)
    fun onShareTextReceived(text: String, title: String)

    class Result<T>(val onResult: (T) -> Unit)
}

class AppModelImpl(private val context: Context) : AppModel {

    override var callback: AppCallbackImpl? = null
    override var close = MutableLiveData<Unit>()
    override var openUrlInNewTab = MutableLiveData<OpenURLInNewTabRequest>()
    override var biometricRequest = MutableLiveData<AppWebViewModel.BiometricRequest>()
    override var authorize = MutableLiveData<AppModel.Result<Completion<String?>>>()
    override var endSession = MutableLiveData<AppModel.Result<LogoutResponse>>()

    override fun reset() {
        close = MutableLiveData()
        openUrlInNewTab = MutableLiveData()
        biometricRequest = MutableLiveData()
        authorize = MutableLiveData()
        endSession = MutableLiveData()
    }

    override fun setBiometricRequest(request: AppWebViewModel.BiometricRequest) {
        onMainThread {
            biometricRequest.value = request
        }
    }

    override fun authorize(onResult: (Completion<String?>) -> Unit) {
        onMainThread {
            authorize.value = AppModel.Result(onResult)
        }
    }

    override fun endSession(onResult: (LogoutResponse) -> Unit) {
        onMainThread {
            endSession.value = AppModel.Result(onResult)
        }
    }

    override fun close() {
        onMainThread {
            close.value = Unit
            callback?.onClose()
        }
    }

    override fun openUrlInNewTab(openURLInNewTabRequest: OpenURLInNewTabRequest) {
        onMainThread {
            openUrlInNewTab.value = openURLInNewTabRequest
            callback?.onOpenInNewTab(openURLInNewTabRequest.url)
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
                        when (completion) {
                            is Success -> {
                                val accessToken = completion.result
                                if (accessToken != null) {
                                    onResult(Success(GetAccessTokenResponse(accessToken, true)))
                                } else {
                                    onResult(Failure(InternalError))
                                }
                            }
                            is Failure -> onResult(Failure(completion.throwable))
                        }
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

    override fun showShareSheet(bitmap: Bitmap) {
        try {
            // Save bitmap to cache directory
            val cachePath = File(context.cacheDir, IMAGES_DIRECTORY_NAME)
            cachePath.mkdirs()

            val receipt = File(cachePath, RECEIPT_FILENAME)
            val stream = FileOutputStream(receipt) // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            // Open share sheet
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.pace_cloud_sdk_file_provider", receipt)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = context.contentResolver.getType(contentUri)
            }

            val chooserIntent = createChooser(shareIntent, null)
            chooserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)

            val resInfoList = context.packageManager.queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            resInfoList.forEach {
                val packageName = it.activityInfo.packageName
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(context, chooserIntent, null)
        } catch (e: FileNotFoundException) {
            Timber.e(e, "Could not create FileOutputStream to write the receipt file")
        } catch (e: IOException) {
            Timber.e(e, "Could not create or save the receipt bitmap")
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "The receipt file is outside the paths supported by the FileProvider")
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "No Activity found to execute the share intent")
        } catch (e: Exception) {
            Timber.e(e, "Could not create, save or share the receipt bitmap")
        }
    }

    override fun showShareSheet(text: String, title: String) {
        try {
            // Open share sheet
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(EXTRA_SUBJECT, title)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TEXT, text)
            }

            val chooserIntent = createChooser(shareIntent, title)
            chooserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)

            startActivity(context, chooserIntent, null)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "No Activity found to execute the share intent")
        } catch (e: Exception) {
            Timber.e(e, "Could not share the shareText")
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

    override fun onShareTextReceived(text: String, title: String) {
        onMainThread {
            callback?.onShareTextReceived(text, title)
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

    override fun isAppRedirectAllowed(app: String, isAllowed: (Boolean) -> Unit) {
        onMainThread {
            callback?.isAppRedirectAllowed(app, isAllowed)
        }
    }

    override fun isSignedIn(isSignedIn: (Boolean) -> Unit) {
        onMainThread {
            callback?.isSignedIn(isSignedIn)
        }
    }

    override fun isRemoteConfigAvailable(isAvailable: (Boolean) -> Unit) {
        onMainThread {
            callback?.isRemoteConfigAvailable(isAvailable)
        }
    }

    companion object {
        private const val IMAGES_DIRECTORY_NAME = "images"
        private const val RECEIPT_FILENAME = "receipt.png"
    }
}
