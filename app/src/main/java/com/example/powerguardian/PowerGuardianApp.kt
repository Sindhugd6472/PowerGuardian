package com.example.powerguardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import java.util.concurrent.TimeUnit

class PowerGuardianApp : Application() {

    companion object {
        const val CHANNEL_ID = "power_guardian_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleBatteryMonitoringWork()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Power Guardian Notifications"
            val descriptionText = "Notifications related to battery and power usage"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleBatteryMonitoringWork() {
        val workRequest = PeriodicWorkRequestBuilder<PowerMonitoringWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PowerMonitoringWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
