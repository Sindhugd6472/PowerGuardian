package com.example.powerguardian

import android.content.Context
import android.util.Log

object UsageClassifier {
    enum class UsageLevel { LOW, MODERATE, HIGH }

    // Use the same timestamped-log interpretation as the UI.
    fun classifyUsage(context: Context): UsageLevel =
        UsageStatsHelper.getUsageLevel(context).first.also {
            Log.d("UsageClassifier", "Current usage level: $it")
        }
}
