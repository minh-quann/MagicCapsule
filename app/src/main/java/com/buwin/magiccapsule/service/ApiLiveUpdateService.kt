package com.buwin.magiccapsule.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences

/**
 * Service listening to external broadcast intents from Tasker, MacroDroid, Termux, etc.
 * to post dynamic custom pills to Honor Magic Capsule.
 */
class ApiLiveUpdateService : Service() {

    private lateinit var capsuleManager: CapsuleManager
    private lateinit var prefs: AppFeaturePreferences

    private val apiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action != ACTION_API_UPDATE) return

            val token = intent.getStringExtra("token")
            if (token != null && token != prefs.apiIslandToken) return

            val capsuleText = intent.getStringExtra("capsule_text") ?: "🌐 API"
            val title = intent.getStringExtra("title") ?: "Thông báo API"
            val details = intent.getStringExtra("details") ?: "Dữ liệu từ webhook bên ngoài"

            capsuleManager.postApiCapsule(capsuleText, title, details)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ForegroundHelper.startForegroundServiceCompat(
            service = this,
            notificationId = 3003,
            title = "API Webhook",
            foregroundServiceType = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        capsuleManager = CapsuleManager(this)
        prefs = AppFeaturePreferences(this)

        val filter = IntentFilter(ACTION_API_UPDATE)
        ContextCompat.registerReceiver(
            this,
            apiReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!prefs.isApiIslandEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(apiReceiver)
        } catch (_: Exception) {}
        capsuleManager.dismiss(CapsuleManager.ID_API)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_API_UPDATE = "com.buwin.magiccapsule.action.API_UPDATE"

        fun start(context: Context) {
            val intent = Intent(context, ApiLiveUpdateService::class.java)
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
            context.stopService(Intent(context, ApiLiveUpdateService::class.java))
        }
    }
}
