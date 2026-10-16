package com.buwin.magiccapsule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.buwin.magiccapsule.R
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.device.DeviceStatsReader
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Monitors device RAM usage, upload/download network speed, and posts real-time stats
 * to Honor Magic Capsule.
 */
class DeviceMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private lateinit var statsReader: DeviceStatsReader
    private lateinit var prefs: AppFeaturePreferences

    companion object {
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, DeviceMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DeviceMonitorService::class.java))
        }
    }

    private lateinit var capsuleManager: CapsuleManager

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        capsuleManager = CapsuleManager(this)
        ForegroundHelper.startForegroundServiceCompat(
            service = this,
            notificationId = CapsuleManager.ID_DEVICE,
            title = "RAM & Tốc độ mạng",
            foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        statsReader = DeviceStatsReader(this)
        prefs = AppFeaturePreferences(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotification = buildNotification("RAM...", "RAM & Tốc độ mạng", "Đang khởi tạo cảm biến...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(CapsuleManager.ID_DEVICE, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(CapsuleManager.ID_DEVICE, initialNotification)
        }

        job?.cancel()
        job = serviceScope.launch {
            while (isActive) {
                val stats = statsReader.readStats()

                val capsuleText = buildString {
                    if (prefs.showRamMetric) append("RAM ${stats.ramUsedPercent}%")
                    if (prefs.showNetworkSpeedMetric) {
                        if (isNotEmpty()) append(" · ")
                        append(stats.combinedSpeedText)
                    }
                    if (prefs.showTemperatureMetric && !prefs.showNetworkSpeedMetric) {
                        if (isNotEmpty()) append(" · ")
                        append(String.format(Locale.US, "%.1f°C", stats.deviceTempC))
                    }
                }.ifBlank { "RAM ${stats.ramUsedPercent}%" }

                val title = String.format(Locale.US, "RAM: %.1fGB / %.1fGB (%d%%)", stats.ramUsedGb, stats.ramTotalGb, stats.ramUsedPercent)
                val details = "↓ ${stats.downloadSpeedText}  ↑ ${stats.uploadSpeedText}  •  ${String.format(Locale.US, "%.1f°C", stats.deviceTempC)}"

                val notification = buildNotification(capsuleText, title, details)
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(CapsuleManager.ID_DEVICE, notification)

                delay(prefs.deviceIslandIntervalMs.coerceIn(1000L, 5000L))
            }
        }

        return START_STICKY
    }

    private fun buildNotification(capsuleText: String, title: String, details: String = ""): Notification {
        return capsuleManager.buildCapsuleNotification(
            channelId = CapsuleManager.CHANNEL_DEVICE,
            capsuleText = capsuleText,
            title = title,
            details = details.ifBlank { "RAM & Mạng thời gian thực" },
            iconRes = R.drawable.ic_ram,
            isOngoing = true
        )
    }

    override fun onDestroy() {
        isRunning = false
        job?.cancel()
        serviceScope.cancel()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(CapsuleManager.ID_DEVICE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
