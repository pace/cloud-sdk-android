package cloud.pace.sdk.appkit.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cloud.pace.sdk.R
import java.util.*

object NotificationUtils {

    private const val CHANNEL_ID = "AppKit Notifications"

    fun sendNotification(context: Context, title: String, text: String = "", @DrawableRes icon: Int, intent: Intent): Int {
        createNotificationChannel(context)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setColor(context.resources.getColor(R.color.pace_blue, context.theme))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val id = Date().time.toInt()
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }

        return id
    }

    fun removeNotification(context: Context, id: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(id)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }
}
