package car.pace.cofu.util

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.inappmessaging.inAppMessaging
import com.google.firebase.messaging.messaging

object AnalyticsUtils {

    fun setAnalyticsEnabled(enabled: Boolean) {
        Firebase.analytics.setAnalyticsCollectionEnabled(enabled)
        Firebase.messaging.isAutoInitEnabled = enabled
        Firebase.inAppMessaging.isAutomaticDataCollectionEnabled = enabled
    }
}
