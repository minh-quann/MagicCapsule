package com.buwin.magiccapsule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import com.buwin.magiccapsule.R

/**
 * Ensures all background services start foreground properly without triggering
 * ForegroundServiceDidNotStartInTimeException on Android 14+ / MagicOS.
 * Uses IMPORTANCE_MIN and shared ID 6300 to remain completely silent and hidden
 * from the active notification shade, exactly matching NoticeFlow architecture.
 */
object ForegroundHelper {

    private const val CHANNEL_SILENT_FOREGROUND = "magic_service_silent"
    const val SHARED_FOREGROUND_ID = 6300

    fun startForegroundServiceCompat(
        service: Service,
        notificationId: Int,
        title: String,
        foregroundServiceType: Int = 0
    ) {
        val nm = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete old low-importance channel if it existed
            try {
                nm.deleteNotificationChannel("magic_service_internal")
            } catch (_: Exception) {}

            var channel = nm.getNotificationChannel(CHANNEL_SILENT_FOREGROUND)
            if (channel == null) {
                channel = NotificationChannel(
                    CHANNEL_SILENT_FOREGROUND,
                    "Dịch vụ nền",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_SECRET
                }
                nm.createNotificationChannel(channel)
            }
        }

        val targetId = SHARED_FOREGROUND_ID

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(service, CHANNEL_SILENT_FOREGROUND)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(service)
        }
            .setSmallIcon(R.drawable.ic_charging)
            .setContentTitle("Dịch vụ nền Magic Capsule")
            .setContentText("Đang chạy ngầm")
            .setOngoing(true)
            .setShowWhen(false)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && foregroundServiceType != 0) {
                service.startForeground(targetId, notification, foregroundServiceType)
            } else {
                service.startForeground(targetId, notification)
            }
        } catch (_: Exception) {
            try {
                service.startForeground(targetId, notification)
            } catch (_: Exception) {}
        }
    }
}
