package com.buwin.magiccapsule.receiver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Listens for Bluetooth accessory connections (earphones, headsets, smartwatches)
 * and displays an Apple/Honor dynamic connection capsule pill.
 */
class BluetoothReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = AppFeaturePreferences(context)
        if (!prefs.isHeadsetIslandEnabled) return

        val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        } ?: return

        val capsuleManager = CapsuleManager(context)

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val deviceName = try {
                    device.name ?: "Tai nghe Bluetooth"
                } catch (_: Exception) {
                    "Tai nghe Bluetooth"
                }
                capsuleManager.postHeadsetCapsule(deviceName, "")

                // Auto dismiss after duration
                CoroutineScope(Dispatchers.Default).launch {
                    delay(prefs.headsetDurationSec * 1000L)
                    capsuleManager.dismiss(CapsuleManager.ID_HEADSET)
                }
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                capsuleManager.dismiss(CapsuleManager.ID_HEADSET)
            }
        }
    }
}
