package car.pace.cofu.features.analytics

import car.pace.cofu.BuildConfig
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.ui.wallet.paymentmethods.PaymentMethodsViewModel
import car.pace.cofu.util.LogAndBreadcrumb
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.inappmessaging.inAppMessaging
import com.google.firebase.messaging.messaging
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class Analytics @Inject constructor(
    val firebaseAnalytics: FirebaseAnalytics,
    val sharedPreferencesRepository: SharedPreferencesRepository
) {
    val userEnabledTracking = sharedPreferencesRepository
        .getValue(SharedPreferencesRepository.PREF_KEY_TRACKING_ENABLED, userEnabledTracking())

    inner class TrackingAppCallback(private val onCloseCallback: (() -> Unit)? = null) : AppCallbackImpl() {
        override fun logEvent(key: String, parameters: Map<String, Any>) {
            super.logEvent(key, parameters)

            when (key) {
                PaymentMethodsViewModel.PAYMENT_METHOD_CREATION_STARTED -> logEvent(CardBoardingStarted)
                PaymentMethodsViewModel.PAYMENT_METHOD_ADDED -> logEvent(CardBoardingDone(parameters["kind"] as? String?))
                else -> {}
            }
        }

        override fun onClose() {
            if (onCloseCallback != null) {
                onCloseCallback.invoke()
            } else {
                super.onClose()
            }
        }
    }

    /**
     * Configure analytics (enables or disables analytics data collection)
     *
     * @return True if analytics is enabled, false otherwise.
     */
    fun initAnalytics(): Boolean {
        return setAnalyticsEnabled(null, userEnabledTracking())
    }

    fun userEnabledTracking() = sharedPreferencesRepository.getBoolean(SharedPreferencesRepository.PREF_KEY_TRACKING_ENABLED, false)

    fun enableAnalyticsFeature(tag: String?, userEnabled: Boolean) {
        sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_TRACKING_ENABLED, userEnabled)
        setAnalyticsEnabled(tag, userEnabled)
    }

    private fun setAnalyticsEnabled(tag: String?, userEnabled: Boolean): Boolean {
        val isEnabled = BuildConfig.ANALYTICS_ENABLED && userEnabled

        firebaseAnalytics.setAnalyticsCollectionEnabled(isEnabled)
        Firebase.messaging.isAutoInitEnabled = isEnabled
        Firebase.inAppMessaging.isAutomaticDataCollectionEnabled = isEnabled

        tag?.let {
            LogAndBreadcrumb.i(it, if (isEnabled) "Analytics enabled" else "Analytics disabled")
        }

        return isEnabled
    }

    fun logEvent(analyticEvent: AnalyticEvent) {
        firebaseAnalytics.logEvent(analyticEvent.key, analyticEvent.parameters)
        Timber.d("Log event ${analyticEvent.key} (params: ${analyticEvent.parameters})")
    }

    fun logAppInstall() {
        if (sharedPreferencesRepository.getBoolean(SharedPreferencesRepository.PREF_KEY_FIRST_RUN, true)) {
            logEvent(AppInstalled)
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_FIRST_RUN, false)
        }
    }
}
