package com.buwin.magiccapsule.data.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.buwin.magiccapsule.data.model.BatteryInfo
import com.buwin.magiccapsule.data.model.PluggedType
import java.io.File
import kotlin.math.abs

/**
 * High-precision battery telemetry reader optimized for Honor MagicOS devices.
 * Extracts real-time current, voltage, temperature, and calculates charging wattage.
 */
class BatteryStatsReader(private val context: Context) {

    private val batteryManager: BatteryManager? by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    }

    private var lastValidCurrentUa: Long = 0L
    private var lastValidVoltageMv: Int = 0
    private var smoothedPowerW: Double? = null

    companion object {
        private val CURRENT_SYSFS_PATHS = arrayOf(
            "/sys/class/power_supply/battery/current_now",
            "/sys/class/power_supply/battery/batt_current_now",
            "/sys/class/power_supply/bms/current_now",
            "/sys/class/power_supply/battery/charger_current",
            "/sys/class/power_supply/battery/input_current_now",
            "/sys/class/power_supply/battery/charge_current",
            "/sys/class/power_supply/main/current_now",
            "/sys/class/power_supply/usb/current_now"
        )

        private val VOLTAGE_SYSFS_PATHS = arrayOf(
            "/sys/class/power_supply/battery/voltage_now",
            "/sys/class/power_supply/battery/batt_vol",
            "/sys/class/power_supply/bms/voltage_now",
            "/sys/class/power_supply/battery/voltage_avg"
        )
    }

    /**
     * Reads current battery telemetry and computes active power wattage.
     */
    fun readBatteryInfo(): BatteryInfo {
        val batteryIntent = getStickyBatteryIntent()

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL

        val rawLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val levelPercent = if (rawLevel >= 0 && scale > 0) (rawLevel * 100 / scale).coerceIn(0, 100) else 0

        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val pluggedType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> PluggedType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PluggedType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PluggedType.WIRELESS
            else -> PluggedType.NONE
        }

        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temperatureC = rawTemp / 10.0

        // Read electrical telemetry
        var currentUa = readCurrentUa()
        var voltageMv = readVoltageMv(batteryIntent)

        if (currentUa != 0L) {
            lastValidCurrentUa = currentUa
        } else if (lastValidCurrentUa != 0L) {
            currentUa = lastValidCurrentUa
        }

        if (voltageMv > 0) {
            lastValidVoltageMv = voltageMv
        } else if (lastValidVoltageMv > 0) {
            voltageMv = lastValidVoltageMv
        }

        val rawCurrentA = abs(currentUa) / 1_000_000.0
        val voltageV = voltageMv / 1_000.0
        val currentMa = (abs(currentUa) / 1_000L)

        val rawPowerW = rawCurrentA * voltageV

        // Smooth power wattage reading using Exponential Moving Average (alpha = 0.6)
        val powerW = if (isCharging && rawPowerW > 0.05) {
            val smoothed = smoothedPowerW?.let { 0.6 * rawPowerW + 0.4 * it } ?: rawPowerW
            smoothedPowerW = smoothed
            smoothed
        } else {
            smoothedPowerW = null
            0.0
        }

        return BatteryInfo(
            powerW = powerW,
            currentMa = currentMa,
            voltageV = voltageV,
            levelPercent = levelPercent,
            temperatureC = temperatureC,
            isCharging = isCharging,
            isFull = isFull,
            pluggedType = pluggedType
        )
    }

    private fun getStickyBatteryIntent(): Intent? {
        return try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) {
            null
        }
    }

    private fun readCurrentUa(): Long {
        // Step 1: Query standard BatteryManager properties
        val bmCurrent = readCurrentFromBatteryManager()
        if (bmCurrent != 0L) return bmCurrent

        // Step 2: Fallback to reading Linux sysfs paths directly
        return readCurrentFromSysfs()
    }

    private fun readCurrentFromBatteryManager(): Long {
        val bm = batteryManager ?: return 0L
        val currentNow = readCurrentProperty(bm, BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentNow != 0L) return currentNow

        return readCurrentProperty(bm, BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
    }

    private fun readCurrentProperty(bm: BatteryManager, propertyId: Int): Long {
        return try {
            val intVal = bm.getIntProperty(propertyId)
            if (intVal != Int.MIN_VALUE && intVal != 0) {
                return normalizeApiCurrent(intVal.toLong())
            }

            val longVal = bm.getLongProperty(propertyId)
            if (longVal != Long.MIN_VALUE && longVal != 0L) {
                return normalizeApiCurrent(longVal)
            }
            0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Normalizes current readings across OEM HAL implementations.
     * Some vendors return values in mA instead of µA.
     */
    private fun normalizeApiCurrent(raw: Long): Long {
        val absVal = abs(raw)
        // If absolute current is between 1 and 50,000, value is reported in mA -> convert to µA
        return if (absVal in 1..50_000) raw * 1_000L else raw
    }

    private fun readCurrentFromSysfs(): Long {
        for (path in CURRENT_SYSFS_PATHS) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    val content = file.readText().trim()
                    val raw = content.toLongOrNull() ?: continue
                    if (raw != 0L) {
                        return normalizeApiCurrent(raw)
                    }
                } catch (_: Exception) {
                    continue
                }
            }
        }
        return 0L
    }

    private fun readVoltageMv(batteryIntent: Intent?): Int {
        // Step 1: Try reading from sysfs for higher precision
        val sysfsMv = readVoltageFromSysfs()
        if (sysfsMv in 2500..9000) return sysfsMv

        // Step 2: Fallback to EXTRA_VOLTAGE in ACTION_BATTERY_CHANGED intent
        val intentVoltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        if (intentVoltage > 0) {
            val normalizedMv = if (intentVoltage > 10_000) intentVoltage / 1000 else intentVoltage
            if (normalizedMv in 2500..9000) {
                return normalizedMv
            }
        }
        return 0
    }

    private fun readVoltageFromSysfs(): Int {
        for (path in VOLTAGE_SYSFS_PATHS) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    val raw = file.readText().trim().toIntOrNull() ?: continue
                    val mv = if (raw > 10_000) raw / 1000 else raw
                    if (mv in 2500..9000) {
                        return mv
                    }
                } catch (_: Exception) {
                    continue
                }
            }
        }
        return 0
    }
}
