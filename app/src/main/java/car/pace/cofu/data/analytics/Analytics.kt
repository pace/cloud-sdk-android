package car.pace.cofu.data.analytics

import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.util.BuildProvider
import car.pace.cofu.util.LogAndBreadcrumb
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.inappmessaging.inAppMessaging
import com.google.firebase.messaging.messaging
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class Analytics @Inject constructor(
    val sharedPreferencesRepository: SharedPreferencesRepository
) {
    val userEnabledTracking = sharedPreferencesRepository
        .getValue(SharedPreferencesRepository.PREF_KEY_TRACKING_ENABLED, userEnabledTracking())

    inner class TrackingAppCallback(private val onCloseCallback: (() -> Unit)? = null) : AppCallbackImpl() {
        override fun logEvent(key: String, parameters: Map<String, Any>, context: Map<String, Any>?) {
            when (key) {
                PAYMENT_METHOD_CREATION_STARTED -> logEvent(CardBoardingStarted)
                PAYMENT_METHOD_ADDED -> logEvent(CardBoardingDone(parameters["kind"] as? String?))
                FuelingEnded.key -> logEvent(FuelingEnded(parameters["success"] as? Boolean))
                FuelingCanceled.key -> logEvent(FuelingCanceled)
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

    fun isTrackingEnabled(userEnabled: Boolean = userEnabledTracking()): Boolean {
        return BuildProvider.isAnalyticsEnabled() && userEnabled
    }

    fun userEnabledTracking(): Boolean {
        return sharedPreferencesRepository.getBoolean(SharedPreferencesRepository.PREF_KEY_TRACKING_ENABLED, false)
    }

    fun enableAnalyticsFeature(tag: String?, userEnabled: Boolean) {
        sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_TRACKING_ENABLED, userEnabled)
        setAnalyticsEnabled(tag, userEnabled)
    }

    private fun setAnalyticsEnabled(tag: String?, userEnabled: Boolean): Boolean {
        // We are only allowed to access the Firebase instance if the google-services.json is present.
        // This is only the case if analytics is enabled, otherwise the app crashes.
        if (BuildProvider.isAnalyticsEnabled()) {
            Firebase.analytics.setAnalyticsCollectionEnabled(userEnabled)
            Firebase.messaging.isAutoInitEnabled = userEnabled
            Firebase.inAppMessaging.isAutomaticDataCollectionEnabled = userEnabled
        }

        val isEnabled = isTrackingEnabled(userEnabled)

        tag?.let {
            LogAndBreadcrumb.i(it, if (isEnabled) "Analytics enabled" else "Analytics disabled")
        }

        return isEnabled
    }

    fun logEvent(analyticEvent: AnalyticEvent) {
        if (BuildProvider.isAnalyticsEnabled()) {
            Firebase.analytics.logEvent(analyticEvent.key, analyticEvent.parameters)
            Timber.d("Log event ${analyticEvent.key} (params: ${analyticEvent.parameters})")
        }
    }

    fun logAppInstall() {
        if (sharedPreferencesRepository.getBoolean(SharedPreferencesRepository.PREF_KEY_FIRST_RUN, true)) {
            logEvent(AppInstalled)
            sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_FIRST_RUN, false)
        }
    }

    companion object {
        const val PAYMENT_METHOD_CREATION_STARTED = "payment_method_creation_started"
        const val PAYMENT_METHOD_ADDED = "payment_method_added"
    }
}
