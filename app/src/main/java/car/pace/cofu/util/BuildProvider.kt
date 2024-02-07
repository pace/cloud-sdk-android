package car.pace.cofu.util

import android.os.Build
import car.pace.cofu.BuildConfig

object BuildProvider {
    fun isAnalyticsEnabled(): Boolean = BuildConfig.ANALYTICS_ENABLED
    fun hidePrices(): Boolean = BuildConfig.HIDE_PRICES
    fun getSDKVersion() = Build.VERSION.SDK_INT
}
