package com.buwin.magiccapsule.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.IBinder
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Periodically queries upcoming events within the next 24 hours from Android Calendar Provider
 * and publishes countdown timer capsule.
 */
class CalendarLiveUpdateService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var pollJob: Job? = null
    private lateinit var capsuleManager: CapsuleManager
    private lateinit var prefs: AppFeaturePreferences

    override fun onCreate() {
        super.onCreate()
        ForegroundHelper.startForegroundServiceCompat(
            service = this,
            notificationId = 3002,
            title = "Đếm ngược lịch",
            foregroundServiceType = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        capsuleManager = CapsuleManager(this)
        prefs = AppFeaturePreferences(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!prefs.isCalendarIslandEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        startPolling()
        return START_STICKY
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                checkNextCalendarEvent()
                delay(30000L) // Check every 30 seconds
            }
        }
    }

    private fun checkNextCalendarEvent() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val now = System.currentTimeMillis()
        val lookAhead = now + 86400000L // 24 hours

        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), lookAhead.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val title = cursor.getString(0) ?: "Sự kiện lịch"
                val dtStart = cursor.getLong(1)
                val diffMin = ((dtStart - now) / 60000L).coerceAtLeast(0)

                val countdownText = when {
                    diffMin == 0L -> "Đang diễn ra"
                    diffMin < 60L -> "$diffMin phút nữa"
                    else -> "${diffMin / 60L}h ${diffMin % 60L}m nữa"
                }

                capsuleManager.postCalendarCapsule(title, countdownText)
            } else {
                capsuleManager.dismiss(CapsuleManager.ID_CALENDAR)
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        capsuleManager.dismiss(CapsuleManager.ID_CALENDAR)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CalendarLiveUpdateService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CalendarLiveUpdateService::class.java))
        }
    }
}
