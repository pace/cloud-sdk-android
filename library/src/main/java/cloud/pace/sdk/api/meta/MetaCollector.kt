package cloud.pace.sdk.api.meta

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.enqueue
import cloud.pace.sdk.utils.requestId
import org.koin.core.component.get
import timber.log.Timber
import java.util.Locale

class MetaCollector(isEnabled: Boolean) : CloudSDKKoinComponent, DefaultLifecycleObserver {

    private val lifecycle = ProcessLifecycleOwner.get().lifecycle
    private val data by lazy {
        MetaCollectorData(
            deviceId = DeviceUtils.getDeviceId(),
            clientId = PACECloudSDK.configuration.clientId,
            locale = Locale.getDefault().toLanguageTag(),
            services = listOf(
                MetaCollectorService(PACECloudSDK.configuration.clientAppName, "${PACECloudSDK.configuration.clientAppVersion}_${PACECloudSDK.configuration.clientAppBuild}"),
                MetaCollectorService("cloud-sdk-android", BuildConfig.VERSION_NAME),
                MetaCollectorService("android", DeviceUtils.getAndroidVersion())
            )
        )
    }

    var isEnabled = isEnabled
        set(value) {
            if (field != value) {
                setActivationState(value)
            }
            field = value
        }

    init {
        setActivationState(isEnabled)
    }

    private fun setActivationState(isEnabled: Boolean) {
        if (isEnabled) {
            lifecycle.addObserver(this)
        } else {
            lifecycle.removeObserver(this)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        sendData()
    }

    fun addData(userId: String? = null, locationData: MetaCollectorLocation? = null, firebasePushToken: String? = null, services: List<MetaCollectorService>? = null, locale: String? = null) {
        data.apply {
            this.userId = userId
            lastLocation = locationData
            this.firebasePushToken = firebasePushToken

            if (services != null) {
                this.services = this.services.plus(services)
            }

            if (locale != null) {
                this.locale = locale
            }
        }
    }

    fun sendData() {
        MetaCollectorAPI.collectData(data).enqueue {
            onResponse = {
                if (it.isSuccessful) {
                    Timber.i("Successfully send meta collector request")
                } else {
                    Timber.w("Meta collector request failed with ${it.code()} (request ID = ${it.requestId})")
                }
            }

            onFailure = {
                Timber.i(it, "Could not send meta collector request")
            }
        }
    }
}
