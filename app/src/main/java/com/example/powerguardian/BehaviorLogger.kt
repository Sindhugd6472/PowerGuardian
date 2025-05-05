package com.example.powerguardian

import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object BehaviorLogger {

    private const val FILE_NAME = "behavior_log.txt"

    fun logEvent(context: Context, event: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val log = "$timestamp - $event\n"
            File(context.filesDir, FILE_NAME).appendText(log)
            Log.d("BehaviorLogger", log)
        } catch (e: Exception) {
            Log.e("BehaviorLogger", "Log failed: ${e.message}", e)
        }
    }

    fun registerScreenEvents(context: Context) {
        val screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> logEvent(ctx, "Screen ON")
                    Intent.ACTION_SCREEN_OFF -> logEvent(ctx, "Screen OFF")
                }
            }
        }
        context.registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    fun registerChargingEvents(context: Context) {
        val chargingReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> logEvent(ctx, "Charger Connected")
                    Intent.ACTION_POWER_DISCONNECTED -> logEvent(ctx, "Charger Disconnected")
                }
            }
        }
        context.registerReceiver(chargingReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        })
    }

    fun logTopApps(context: Context) {
        try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - TimeUnit.HOURS.toMillis(1),
                now
            )
            if (stats.isNullOrEmpty()) {
                logEvent(context, "No Usage Stats Available — Permission Required")
                return
            }
            val topApps = stats.sortedByDescending { it.totalTimeInForeground }
                .take(5)
                .map { it.packageName }
            logEvent(context, "Top 5 Apps (Last Hour): ${topApps.joinToString(", ")}")
        } catch (e: Exception) {
            logEvent(context, "Error logging top apps: ${e.message}")
        }
    }
}
