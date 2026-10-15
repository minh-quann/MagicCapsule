package com.buwin.magiccapsule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buwin.magiccapsule.service.KeepAliveScheduler

/**
 * Handles periodic AlarmManager ticks to ensure background tasks remain healthy.
 */
class KeepAliveTickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        KeepAliveScheduler.recoverServices(context)
    }

    companion object {
        const val ACTION_TICK = "com.buwin.magiccapsule.KEEP_ALIVE_TICK"
    }
}
