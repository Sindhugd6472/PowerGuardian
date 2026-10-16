package com.example.powerguardian

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object UsageStatsHelper {
    fun readBehaviorLog(context: Context): String {
        val file = File(context.filesDir, "behavior_log.txt")
        return if (file.exists()) file.readText() else "No logs found."
    }

    fun getUsageLevel(context: Context): Pair<UsageClassifier.UsageLevel, Int> {
        val file = File(context.filesDir, "behavior_log.txt")
        if (!file.exists()) return UsageClassifier.UsageLevel.LOW to 0

        val currentHour = SimpleDateFormat("yyyy-MM-dd HH:", Locale.getDefault()).format(Date())
        // Match the logger's zero-padded timestamp and today's date. Ignore app-list
        // continuation lines when calculating the fraction of timestamped events.
        val entries = file.readLines().filter {
            it.matches(Regex("""\d{4}-\d{2}-\d{2} \d{2}:.*""")) ||
                it.startsWith("Top 5 Apps as of ")
        }
        val countInHour = entries.count {
            it.startsWith(currentHour) || it.startsWith("Top 5 Apps as of $currentHour")
        }
        val percent = (countInHour * 100) / entries.size.coerceAtLeast(1)

        val level = when {
            percent >= 70 -> UsageClassifier.UsageLevel.HIGH
            percent >= 30 -> UsageClassifier.UsageLevel.MODERATE
            else -> UsageClassifier.UsageLevel.LOW
        }

        return level to percent
    }



    fun logTopUsedApps(context: Context) {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24) // Last 24 hours

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList == null || usageStatsList.isEmpty()) {
            Log.w("UsageStatsHelper", "No usage stats available.")
            return
        }

        val topApps = usageStatsList
            .sortedByDescending { it.totalTimeInForeground }
            .take(5)
            .map {
                val minutes = it.totalTimeInForeground / 1000 / 60
                "${it.packageName} - ${minutes} min"
            }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logContent = buildString {
            append("Top 5 Apps as of $timestamp:\n")
            topApps.forEachIndexed { index, entry ->
                append("${index + 1}. $entry\n")
            }
            append("\n")
        }

        // Log to Logcat
        Log.i("TopApps", logContent)

        // Save to file
        try {
            val file = File(context.filesDir, "behavior_log.txt")
            file.appendText(logContent)
        } catch (e: Exception) {
            Log.e("UsageStatsHelper", "Error writing to file", e)
        }
    }
}
