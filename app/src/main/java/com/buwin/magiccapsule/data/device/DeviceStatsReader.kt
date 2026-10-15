package com.buwin.magiccapsule.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import java.util.Locale

data class DeviceStats(
    val ramUsedPercent: Int,
    val ramUsedGb: Double,
    val ramTotalGb: Double,
    val downloadSpeedText: String,
    val uploadSpeedText: String,
    val combinedSpeedText: String,
    val deviceTempC: Double
)

/**
 * Reads real-time hardware telemetry: RAM usage, Network I/O speed, and Temperature.
 */
class DeviceStatsReader(private val context: Context) {

    private val activityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastTimestamp = System.currentTimeMillis()

    fun readStats(): DeviceStats {
        val now = System.currentTimeMillis()
        val elapsedSec = ((now - lastTimestamp) / 1000.0).coerceAtLeast(0.1)

        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()

        val rxDelta = if (lastRxBytes > 0 && currentRx >= lastRxBytes) currentRx - lastRxBytes else 0L
        val txDelta = if (lastTxBytes > 0 && currentTx >= lastTxBytes) currentTx - lastTxBytes else 0L

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastTimestamp = now

        val rxBytesPerSec = (rxDelta / elapsedSec).toLong()
        val txBytesPerSec = (txDelta / elapsedSec).toLong()

        val downloadSpeed = formatSpeed(rxBytesPerSec)
        val uploadSpeed = formatSpeed(txBytesPerSec)
        val combinedSpeed = if (rxBytesPerSec >= txBytesPerSec) "↓ $downloadSpeed" else "↑ $uploadSpeed"

        // RAM Info
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val totalBytes = memInfo.totalMem.toDouble()
        val availBytes = memInfo.availMem.toDouble()
        val usedBytes = totalBytes - availBytes
        val ramPercent = ((usedBytes / totalBytes) * 100).toInt().coerceIn(0, 100)
        val ramUsedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)
        val ramTotalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)

        // Device Temp (Battery probe)
        val tempIntent = try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) { null }
        val rawTemp = tempIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val deviceTempC = rawTemp / 10.0

        return DeviceStats(
            ramUsedPercent = ramPercent,
            ramUsedGb = ramUsedGb,
            ramTotalGb = ramTotalGb,
            downloadSpeedText = downloadSpeed,
            uploadSpeedText = uploadSpeed,
            combinedSpeedText = combinedSpeed,
            deviceTempC = deviceTempC
        )
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format(Locale.US, "%d KB/s", bytesPerSec / 1024)
            else -> "$bytesPerSec B/s"
        }
    }
}
