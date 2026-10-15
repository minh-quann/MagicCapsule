package com.buwin.magiccapsule.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import com.buwin.magiccapsule.receiver.KeepAliveTickReceiver

/**
 * Periodically ensures all services and notification listeners are alive and bonded.
 */
object KeepAliveScheduler {

    private const val REQUEST_CODE = 0x6b110001
    private const val INTERVAL_MS = 240000L // 4 minutes

    fun scheduleNextTick(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, KeepAliveTickReceiver::class.java).apply {
                action = KeepAliveTickReceiver.ACTION_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Safeguard against SecurityException on Android 12+ if SCHEDULE_EXACT_ALARM is not granted
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: Exception) {
            // Silently ignore if system denies scheduling alarm
        }
    }

    fun recoverServices(context: Context) {
        try {
            val component = ComponentName(context, MagicNotificationListenerService::class.java)
            NotificationListenerService.requestRebind(component)
        } catch (_: Exception) {}
        try {
            scheduleNextTick(context)
        } catch (_: Exception) {}
    }
}
