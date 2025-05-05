package com.example.powerguardian

import android.content.Context
import android.util.Log
import java.io.File
import java.time.LocalTime

object UsageClassifier {

    enum class UsageLevel {
        LOW, MODERATE, HIGH
    }

    fun classifyUsage(context: Context): UsageLevel {
        val file = File(context.filesDir, "behavior_log.txt")
        if (!file.exists()) return UsageLevel.LOW

        val logLines = file.readLines()
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)


        // Extract recent entries (last 1 hour) — simple heuristic
        val recentApps = logLines.filter {
            it.contains("hour=$currentHour")
        }.flatMap {
            Regex("package=([\\w.]+)").findAll(it).map { match -> match.groupValues[1] }
        }

        val appFrequency = recentApps.groupingBy { it }.eachCount()

        return when {
            appFrequency.size >= 5 -> UsageLevel.HIGH
            appFrequency.size in 2..4 -> UsageLevel.MODERATE
            else -> UsageLevel.LOW
        }.also {
            Log.d("UsageClassifier", "Current usage level: $it")
        }
    }
}
