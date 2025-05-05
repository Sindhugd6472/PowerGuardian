package com.example.powerguardian

import android.app.Activity
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.powerguardian.ui.theme.PowerGuardianTheme
import java.util.concurrent.TimeUnit
import com.example.powerguardian.UsageStatsHelper.getUsageLevel

enum class ScreenState {
    MAIN, BEHAVIOR_LOG
}

class MainActivity : ComponentActivity() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BehaviorLogger.registerScreenEvents(this)
        BehaviorLogger.registerChargingEvents(this)

        setContent {
            var screenState by remember { mutableStateOf(ScreenState.MAIN) }

            PowerGuardianTheme {
                when (screenState) {
                    ScreenState.MAIN -> PowerManagementUI(
                        onAnalyzeClick = {
                            UsageStatsHelper.logTopUsedApps(this)
                            screenState = ScreenState.BEHAVIOR_LOG
                        }
                    )
                    ScreenState.BEHAVIOR_LOG -> BehaviorLogScreen(
                        context = this,
                        onBackClick = { screenState = ScreenState.MAIN }
                    )
                }
            }
        }
    }

    @Composable
    fun PowerManagementUI(onAnalyzeClick: () -> Unit) {
        var batteryMonitoring by remember { mutableStateOf(false) }
        var autoModeEnabled by remember { mutableStateOf(true) }

        val (usageLevel, percentage) = getUsageLevel(this)

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PowerGuardian", style = MaterialTheme.typography.headlineMedium)

                Text(
                    text = "Current usage level: ${usageLevel.name} (${percentage}%)",
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto Mode")
                    Switch(checked = autoModeEnabled, onCheckedChange = {
                        autoModeEnabled = it
                        showToast("Auto Mode: ${if (it) "Enabled" else "Disabled"}")
                    })
                }

                LaunchedEffect(autoModeEnabled, usageLevel) {
                    if (autoModeEnabled) {
                        when (usageLevel) {
                            UsageClassifier.UsageLevel.HIGH -> {
                                Log.d("AutoMode", "HIGH usage detected → Acquire WakeLock & Start Service")
                                acquireWakeLock()
                                ContextCompat.startForegroundService(
                                    this@MainActivity,
                                    Intent(this@MainActivity, PowerManagementService::class.java)
                                )
                            }

                            UsageClassifier.UsageLevel.MODERATE -> {
                                Log.d("AutoMode", "MODERATE usage → Start Battery Monitoring")
                                startBatteryMonitoring()
                            }

                            UsageClassifier.UsageLevel.LOW -> {
                                Log.d("AutoMode", "LOW usage → Release WakeLock & Stop Service")
                                releaseWakeLock()
                                stopService(Intent(this@MainActivity, PowerManagementService::class.java))
                            }
                        }
                    }
                }

                Button(onClick = {
                    acquireWakeLock()
                    showToast("WakeLock acquired")
                }) { Text("Acquire WakeLock") }

                Button(onClick = {
                    releaseWakeLock()
                    showToast("WakeLock released")
                }) { Text("Release WakeLock") }

                Button(onClick = {
                    ContextCompat.startForegroundService(
                        this@MainActivity,
                        Intent(this@MainActivity, PowerManagementService::class.java)
                    )
                    showToast("PowerManagementService started")
                }) { Text("Start Power Service") }

                Button(onClick = {
                    stopService(Intent(this@MainActivity, PowerManagementService::class.java))
                    showToast("PowerManagementService stopped")
                }) { Text("Stop Power Service") }

                Button(onClick = {
                    startBatteryMonitoring()
                    batteryMonitoring = true
                    showToast("Battery Monitoring Started")
                }) { Text("Start Battery Monitoring") }

                Button(onClick = {
                    startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }) { Text("Disable Battery Optimization") }

                Button(onClick = {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }) { Text("Grant Usage Access") }

                Button(onClick = {
                    startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                }) { Text("Open Power Settings") }

                Button(onClick = {
                    BehaviorLogger.logTopApps(this@MainActivity)
                    showToast("Top 5 apps logged to Logcat & file")
                }) { Text("Log Top 5 Used Apps") }

                Button(onClick = onAnalyzeClick) {
                    Text("Analyze App Usage")
                }
            }
        }
    }

    @Composable
    fun BehaviorLogScreen(context: Context, onBackClick: () -> Unit) {
        val logText = remember { UsageStatsHelper.readBehaviorLog(context) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(onClick = onBackClick) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Behavior Log", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = logText)
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PowerGuardian::WakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
    }

    private fun startBatteryMonitoring() {
        val request = PeriodicWorkRequestBuilder<PowerMonitoringWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PowerMonitoringWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



