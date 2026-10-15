package com.buwin.magiccapsule.service

import android.app.KeyguardManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Monitors lockscreen and face unlock events to display dynamic capsule status.
 */
class FaceUnlockMonitorService : Service() {

    private lateinit var capsuleManager: CapsuleManager
    private lateinit var prefs: AppFeaturePreferences
    private val scope = CoroutineScope(Dispatchers.Main)
    private var dismissJob: Job? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                Intent.ACTION_USER_PRESENT -> {
                    // Face unlock completed / unlocked
                    capsuleManager.postFaceUnlockCapsule(isUnlocked = true)
                    dismissJob?.cancel()
                    dismissJob = scope.launch {
                        delay(2000L)
                        capsuleManager.dismiss(CapsuleManager.ID_FACE_UNLOCK)
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (km?.isKeyguardLocked == true) {
                        capsuleManager.postFaceUnlockCapsule(isUnlocked = false)
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    capsuleManager.dismiss(CapsuleManager.ID_FACE_UNLOCK)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ForegroundHelper.startForegroundServiceCompat(
            service = this,
            notificationId = 3001,
            title = "Mở khóa khuôn mặt",
            foregroundServiceType = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        capsuleManager = CapsuleManager(this)
        prefs = AppFeaturePreferences(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!prefs.isFaceUnlockIslandEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {}
        capsuleManager.dismiss(CapsuleManager.ID_FACE_UNLOCK)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FaceUnlockMonitorService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FaceUnlockMonitorService::class.java))
        }
    }
}
