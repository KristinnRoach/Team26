package com.example.team26

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.team26.di.AppModule
import com.example.team26.notifications.NotificationConstants
import com.example.team26.workers.StationStateCheckWorker
import java.util.concurrent.TimeUnit

class PilotApp : Application() {

    companion object {
        private const val TAG = "PilotApp"
    }

    override fun onCreate() {
        super.onCreate()

        AppModule.initialize(this)
        try {
            val workManagerConfig = Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build()
            WorkManager.initialize(this, workManagerConfig)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "WorkManager already initialized")
        }

        createNotificationChannel()
        scheduleStationCheckWorker()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationConstants.CHANNEL_ID,
            NotificationConstants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = NotificationConstants.CHANNEL_DESCRIPTION
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleStationCheckWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<StationStateCheckWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            StationStateCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}