package com.buwin.magiccapsule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.data.preferences.CapsulePreferences
import com.buwin.magiccapsule.service.ChargingCapsuleService

/**
 * Handles device charger plug and unplug lifecycle events.
 * Automatically wakes or dismisses the Magic Capsule service with 0% idle battery consumption.
 */
class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = CapsulePreferences(context)
        val featurePrefs = AppFeaturePreferences(context)

        val isEnabled = prefs.autoStartOnPlug && featurePrefs.isPowerIslandEnabled

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (isEnabled) {
                    ChargingCapsuleService.start(context)
                }
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                ChargingCapsuleService.stop(context)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                if (isEnabled && isDeviceCharging(context)) {
                    ChargingCapsuleService.start(context)
                }
            }
        }
    }

    private fun isDeviceCharging(context: Context): Boolean {
        return try {
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } catch (_: Exception) {
            false
        }
    }
}
