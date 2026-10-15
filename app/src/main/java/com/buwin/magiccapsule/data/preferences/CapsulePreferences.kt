package com.buwin.magiccapsule.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class CapsuleFormat(val displayName: String) {
    WATTAGE_ONLY("Chỉ công suất (35.8W)"),
    WATTAGE_AND_CURRENT("Công suất & Dòng điện (35.8W · 4.2A)"),
    WATTAGE_AND_TEMP("Công suất & Nhiệt độ (35.8W · 38°C)")
}

/**
 * Persistence layer for Magic Capsule settings.
 */
class CapsulePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("magic_capsule_prefs", Context.MODE_PRIVATE)

    var autoStartOnPlug: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START, value).apply()

    var autoDismissWhenFull: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DISMISS_FULL, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DISMISS_FULL, value).apply()

    var updateIntervalMs: Long
        get() = prefs.getLong(KEY_UPDATE_INTERVAL, 1000L)
        set(value) = prefs.edit().putLong(KEY_UPDATE_INTERVAL, value).apply()

    var showCurrent: Boolean
        get() = prefs.getBoolean(KEY_SHOW_CURRENT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_CURRENT, value).apply()

    var showVoltage: Boolean
        get() = prefs.getBoolean(KEY_SHOW_VOLTAGE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_VOLTAGE, value).apply()

    var showTemperature: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TEMP, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TEMP, value).apply()

    var showBatteryPercent: Boolean
        get() = prefs.getBoolean(KEY_SHOW_PERCENT, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_PERCENT, value).apply()

    var capsuleFormat: CapsuleFormat
        get() {
            val name = prefs.getString(KEY_CAPSULE_FORMAT, CapsuleFormat.WATTAGE_AND_CURRENT.name)
            return try {
                CapsuleFormat.valueOf(name ?: CapsuleFormat.WATTAGE_AND_CURRENT.name)
            } catch (_: Exception) {
                CapsuleFormat.WATTAGE_AND_CURRENT
            }
        }
        set(value) = prefs.edit().putString(KEY_CAPSULE_FORMAT, value.name).apply()

    companion object {
        private const val KEY_AUTO_START = "auto_start_on_plug"
        private const val KEY_AUTO_DISMISS_FULL = "auto_dismiss_when_full"
        private const val KEY_UPDATE_INTERVAL = "update_interval_ms"
        private const val KEY_SHOW_CURRENT = "show_current"
        private const val KEY_SHOW_VOLTAGE = "show_voltage"
        private const val KEY_SHOW_TEMP = "show_temperature"
        private const val KEY_SHOW_PERCENT = "show_battery_percent"
        private const val KEY_CAPSULE_FORMAT = "capsule_format"
    }
}
