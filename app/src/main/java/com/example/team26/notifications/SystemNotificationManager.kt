package com.example.team26.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.team26.MainActivity
import com.example.team26.R

class SystemNotificationManager(private val context: Context) {

    fun showSummaryNotification(title: String, summaryText: String, details: List<String>) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText(summaryText)
        details.forEach { notificationStyle.addLine(it) }

        val builder = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Update icon
            .setContentTitle(title)
            .setContentText(summaryText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(notificationStyle)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    android.util.Log.w("NotificationManager", "Permission not granted.")
                    return
                }
            }
            notify(NotificationConstants.SUMMARY_NOTIFICATION_ID, builder.build())
        }
    }
}