package car.pace.cofu.util

import android.util.Log
import car.pace.cofu.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import timber.log.Timber

object LogAndBreadcrumb {
    fun i(category: String, message: String) {
        Timber.i(message)
        sendBreadcrumb(category, message, SentryLevel.INFO)
    }

    fun d(category: String, message: String) {
        Timber.d(message)
        sendBreadcrumb(category, message, SentryLevel.DEBUG)
    }

    fun w(throwable: Throwable?, category: String, message: String) {
        Timber.w(throwable, message)
        sendBreadcrumb(category, message, SentryLevel.WARNING, throwable)
    }

    fun e(throwable: Throwable?, category: String, message: String) {
        Timber.e(throwable, message)
        sendBreadcrumb(category, message, SentryLevel.ERROR, throwable)
    }

    fun wtf(throwable: Throwable?, category: String, message: String) {
        Timber.wtf(throwable, message)
        sendBreadcrumb(category, message, SentryLevel.FATAL, throwable)
    }

    fun log(priority: Int, throwable: Throwable?, category: String, message: String) {
        Timber.log(priority, throwable, message)
        val sentryLevel = when (priority) {
            Log.DEBUG -> SentryLevel.DEBUG
            Log.WARN -> SentryLevel.WARNING
            Log.ERROR -> SentryLevel.ERROR
            else -> SentryLevel.INFO
        }
        sendBreadcrumb(category, message, sentryLevel, throwable)
    }

    private fun sendBreadcrumb(category: String, message: String, level: SentryLevel, throwable: Throwable? = null) {
        if (BuildConfig.FIREBASE_ENABLED) {
            FirebaseCrashlytics.getInstance().log(message)
        }

        if (BuildConfig.SENTRY_ENABLED) {
            val breadcrumbMessage = if (throwable?.message != null) {
                "$message: ${throwable.message}"
            } else {
                message
            }

            val breadcrumb = Breadcrumb().apply {
                this.category = category
                this.message = breadcrumbMessage
                this.level = level
            }
            Sentry.addBreadcrumb(breadcrumb)
        }
    }
}
