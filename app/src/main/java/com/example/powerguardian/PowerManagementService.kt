package com.example.powerguardian

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PowerManagementService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // You can add logic to monitor system power events here in Step 2 or later
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "power_management_channel"
        val channelName = "Power Management Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used by PowerGuardian to persist background monitoring"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PowerGuardian Active")
            .setContentText("Monitoring power usage in background")
            .setSmallIcon(R.drawable.ic_battery)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
