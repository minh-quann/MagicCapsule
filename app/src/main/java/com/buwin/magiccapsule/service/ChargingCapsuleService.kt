package com.buwin.magiccapsule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.buwin.magiccapsule.MainActivity
import com.buwin.magiccapsule.R
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.battery.BatteryStatsReader
import com.buwin.magiccapsule.data.model.BatteryInfo
import com.buwin.magiccapsule.data.model.PluggedType
import com.buwin.magiccapsule.data.preferences.CapsuleFormat
import com.buwin.magiccapsule.data.preferences.CapsulePreferences
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
 * Foreground service providing real-time charging wattage and battery metrics
 * directly hooked into Honor Magic Capsule (灵动胶囊) notification pill.
 */
class ChargingCapsuleService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollingJob: Job? = null

    private lateinit var batteryStatsReader: BatteryStatsReader
    private lateinit var preferences: CapsulePreferences
    private lateinit var notificationManager: NotificationManager

    private var isBatteryReceiverRegistered = false
    private var isForceTestMode = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateCapsule()
            }
        }
    }

    companion object {
        const val CHANNEL_ID = CapsuleManager.CHANNEL_CHARGING
        const val NOTIFICATION_ID = CapsuleManager.ID_CHARGING
        const val ACTION_START = "com.buwin.magiccapsule.action.START"
        const val ACTION_STOP = "com.buwin.magiccapsule.action.STOP"
        const val ACTION_FORCE_TEST = "com.buwin.magiccapsule.action.FORCE_TEST"

        var isRunning: Boolean = false
            private set

        fun start(context: Context, forceTest: Boolean = false) {
            val intent = Intent(context, ChargingCapsuleService::class.java).apply {
                action = if (forceTest) ACTION_FORCE_TEST else ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ChargingCapsuleService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var capsuleManager: CapsuleManager

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        capsuleManager = CapsuleManager(this)
        ForegroundHelper.startForegroundServiceCompat(
            service = this,
            notificationId = NOTIFICATION_ID,
            title = "Công suất sạc pin",
            foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        batteryStatsReader = BatteryStatsReader(this)
        preferences = CapsulePreferences(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        registerBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopCapsule()
            return START_NOT_STICKY
        }

        isForceTestMode = intent?.action == ACTION_FORCE_TEST

        val initialInfo = batteryStatsReader.readBatteryInfo()
        val initialNotification = buildNotification(initialInfo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        startPollingLoop()
        return START_STICKY
    }

    private fun startPollingLoop() {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            while (isActive) {
                updateCapsule()
                val interval = preferences.updateIntervalMs.coerceIn(500L, 5000L)
                delay(interval)
            }
        }
    }

    private fun updateCapsule() {
        val info = batteryStatsReader.readBatteryInfo()

        // If not in force test mode and device stopped charging, dismiss automatically
        if (!isForceTestMode && !info.isCharging) {
            stopCapsule()
            return
        }

        // If full and auto dismiss enabled, shut down cleanly
        if (preferences.autoDismissWhenFull && info.isFull && !isForceTestMode) {
            stopCapsule()
            return
        }

        val notification = buildNotification(info)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(info: BatteryInfo): Notification {
        val powerStr = String.format(Locale.US, "%.1fW", if (info.powerW > 0.0) info.powerW else 0.0)
        val tempStr = String.format(Locale.US, "%.1f°C", info.temperatureC)
        val voltStr = String.format(Locale.US, "%.2fV", info.voltageV)
        val currentStr = "${info.currentMa}mA"

        // Construct concise text displayed directly on Honor Magic Capsule pill
        val capsuleTitle = when (preferences.capsuleFormat) {
            CapsuleFormat.WATTAGE_ONLY -> powerStr
            CapsuleFormat.WATTAGE_AND_CURRENT -> "$powerStr · $currentStr"
            CapsuleFormat.WATTAGE_AND_TEMP -> "$powerStr · $tempStr"
        }

        // Expanded content details
        val details = buildList {
            if (preferences.showBatteryPercent) add("${info.levelPercent}%")
            if (preferences.showVoltage && info.voltageV > 0.0) add(voltStr)
            if (preferences.showCurrent && info.currentMa > 0) add(currentStr)
            if (preferences.showTemperature && info.temperatureC > 0.0) add(tempStr)
            val pluggedLabel = when (info.pluggedType) {
                PluggedType.AC -> "Củ sạc nhanh"
                PluggedType.USB -> "Cổng USB"
                PluggedType.WIRELESS -> "Sạc không dây"
                PluggedType.NONE -> if (isForceTestMode) "Chế độ thử nghiệm" else "Pin"
            }
            add(pluggedLabel)
        }.joinToString("  |  ")

        val titleText = if (info.isCharging || isForceTestMode) {
            "⚡ Đang sạc · $powerStr"
        } else {
            "Pin · ${info.levelPercent}%"
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ChargingCapsuleService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return capsuleManager.buildCapsuleNotification(
            channelId = CapsuleManager.CHANNEL_CHARGING,
            capsuleText = capsuleTitle,
            title = titleText,
            details = details,
            iconRes = R.drawable.ic_charging,
            contentIntent = pendingOpenApp,
            isOngoing = true
        )
    }

    private fun registerBatteryReceiver() {
        if (!isBatteryReceiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(batteryReceiver, filter)
            isBatteryReceiverRegistered = true
        }
    }

    private fun unregisterBatteryReceiver() {
        if (isBatteryReceiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}
            isBatteryReceiverRegistered = false
        }
    }

    private fun stopCapsule() {
        isRunning = false
        pollingJob?.cancel()
        serviceScope.cancel()
        unregisterBatteryReceiver()
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        isRunning = false
        pollingJob?.cancel()
        serviceScope.cancel()
        unregisterBatteryReceiver()
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
