package com.buwin.magiccapsule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buwin.magiccapsule.service.KeepAliveScheduler

/**
 * Resumes and refreshes state when screen turns on or user unlocks the device.
 */
class ScreenOnRecoveryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_USER_PRESENT) {
            KeepAliveScheduler.recoverServices(context)
        }
    }
}
