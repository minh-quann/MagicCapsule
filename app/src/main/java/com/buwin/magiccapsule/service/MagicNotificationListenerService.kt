package com.buwin.magiccapsule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.buwin.magiccapsule.data.notification.NotificationChannelRepository
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.notification.CapsuleNotificationType
import com.buwin.magiccapsule.data.notification.NotificationClassifier
import com.buwin.magiccapsule.data.notification.NotificationExtractor
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.data.sms.SmsOtpParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced NotificationListenerService handling:
 * 1. Screen recorder island (Honor Screen Recorder / Huawei)
 * 2. Media playback & lyrics island (Spotify, Apple Music, Zing MP3...)
 * 3. SMS & OTP code interception with 1-tap copy
 * 4. Chat, Call & app notification forwarding to the Magic Capsule (Zalo, Telegram, Messenger...)
 * 5. Lifecycle connection tracking and auto-rebind
 */
class MagicNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var capsuleManager: CapsuleManager
    private lateinit var prefs: AppFeaturePreferences

    private val appLabelCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private var notifDismissJob: kotlinx.coroutines.Job? = null
    private var smsDismissJob: kotlinx.coroutines.Job? = null

    private var lastNotifKey: String = ""
    private var lastNotifText: String = ""
    private var lastNotifTimeMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        instance = this
        capsuleManager = CapsuleManager(this)
        prefs = AppFeaturePreferences(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isListenerConnected = true
        KeepAliveScheduler.scheduleNextTick(this)
    }

    override fun onListenerDisconnected() {
        isListenerConnected = false
        super.onListenerDisconnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(ComponentName(this, MagicNotificationListenerService::class.java))
            } catch (_: Exception) {}
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkgName = sbn.packageName ?: ""
        if (pkgName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // ── 1. ULTRA-FAST PATH: App Notification & Call Forwarding ──
        if (prefs.isNotificationIslandEnabled && prefs.isAppAllowedForCapsule(pkgName)) {
            val channelId = notification.channelId ?: ""

            // Dynamic Per-app real channel filter
            if (channelId.isNotBlank() && !prefs.isAppChannelAllowed(pkgName, channelId)) {
                return
            }

            val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val extractedTitle = NotificationExtractor.extractTitle(notification)
            val extractedText = NotificationExtractor.extractText(notification)
            val title = extractedTitle.ifBlank { rawTitle }
            val text = extractedText.ifBlank { rawText }

            if (title.isBlank() && text.isBlank()) {
                return
            }

            // Fast cached app label lookup
            val appLabel = appLabelCache.getOrPut(pkgName) {
                try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkgName.substringAfterLast('.')
                }
            }

            val currentTime = System.currentTimeMillis()
            val isDuplicate = (sbn.key == lastNotifKey && text == lastNotifText && (currentTime - lastNotifTimeMs < 1500L))
            lastNotifKey = sbn.key
            lastNotifText = text
            lastNotifTimeMs = currentTime

            if (!isDuplicate) {
                val notifType = NotificationClassifier.classify(sbn)
                if (notifType == CapsuleNotificationType.CALL) {
                    capsuleManager.postCallCapsule(
                        packageName = pkgName,
                        appName = appLabel,
                        callerName = title.ifBlank { text }.ifBlank { "Cuộc gọi đến" },
                        callStatus = text.ifBlank { "Đang đổ chuông..." },
                        originalContentIntent = notification.contentIntent,
                        sourceKey = sbn.key
                    )
                } else {
                    capsuleManager.postNotificationCapsule(
                        packageName = pkgName,
                        appName = appLabel,
                        title = title,
                        text = text,
                        originalContentIntent = notification.contentIntent,
                        sourceKey = sbn.key
                    )
                }

                // Cancel system notification so only Magic Capsule shows up
                if (notifType != CapsuleNotificationType.CALL && !sbn.isOngoing) {
                    try {
                        cancelNotification(sbn.key)
                    } catch (e: Exception) {
                        Log.w("MagicNotificationListener", "Failed to cancel system notification: ${e.message}")
                    }
                }

                // Auto dismiss capsule after user configured duration (canceling prior timer so newer messages aren't cut short)
                notifDismissJob?.cancel()
                notifDismissJob = serviceScope.launch {
                    delay(prefs.notificationDurationSec * 1000L)
                    capsuleManager.dismiss(CapsuleManager.ID_NOTIFY)
                    capsuleManager.dismiss(CapsuleManager.ID_CALL)
                }
            }

            // Asynchronously record channel for learning in background without blocking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId.isNotBlank()) {
                serviceScope.launch(Dispatchers.IO) {
                    try {
                        NotificationChannelRepository.recordSeenChannel(this@MagicNotificationListenerService, pkgName, channelId, null)
                    } catch (_: Exception) {}
                }
            }
            return
        }

        // ── 2. Other Features (Honor Screen Recorder, SMS OTP, Media Playback) ──
        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val extractedTitle = NotificationExtractor.extractTitle(notification)
        val extractedText = NotificationExtractor.extractText(notification)
        val title = extractedTitle.ifBlank { rawTitle }
        val text = extractedText.ifBlank { rawText }

        // Screen Recorder detection
        if (ScreenRecordIslandService.isScreenRecorderPackage(pkgName)) {
            val titleLower = title.lowercase()
            val textLower = text.lowercase()
            if (titleLower.contains("kết thúc") || textLower.contains("kết thúc") ||
                titleLower.contains("finish") || textLower.contains("finish") ||
                titleLower.contains("dừng") || textLower.contains("stop")) {
                ScreenRecordIslandService.onRecordStopped(this)
            } else {
                ScreenRecordIslandService.onRecordStarted(this)
            }
            return
        }

        // SMS OTP or Parcel delivery codes
        if (prefs.isSmsIslandEnabled && (title.isNotBlank() || text.isNotBlank())) {
            val combined = "$title $text"
            val otpResult = SmsOtpParser.parse(title.ifBlank { pkgName }, combined)
            if (otpResult != null) {
                capsuleManager.postSmsOtpCapsule(
                    code = otpResult.code,
                    typeLabel = otpResult.type.label,
                    sender = otpResult.sender
                )
                smsDismissJob?.cancel()
                smsDismissJob = serviceScope.launch {
                    delay(prefs.smsCapsuleDurationSec * 1000L)
                    capsuleManager.dismiss(CapsuleManager.ID_SMS)
                }
                return
            }
        }

        // Media Playback (Spotify, YouTube Music, Zing MP3, Apple Music...)
        val isMediaNotification = extras.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
                pkgName.contains("spotify", ignoreCase = true) ||
                pkgName.contains("music", ignoreCase = true) ||
                pkgName.contains("zing", ignoreCase = true)

        if (prefs.isLyricsIslandEnabled && isMediaNotification && title.isNotBlank()) {
            val capsuleText = "🎵 ${title.take(14)}"
            capsuleManager.postMediaCapsule(
                capsuleText = capsuleText,
                title = title,
                artist = text.ifBlank { "Đang phát nhạc" },
                packageName = pkgName,
                originalContentIntent = notification.contentIntent
            )
            return
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkgName = sbn.packageName ?: ""
        if (ScreenRecordIslandService.isScreenRecorderPackage(pkgName)) {
            ScreenRecordIslandService.onRecordStopped(this)
        } else if (pkgName.contains("spotify") || pkgName.contains("music")) {
            capsuleManager.dismiss(CapsuleManager.ID_MEDIA)
        }

        // When a call ends or is dismissed, remove call capsule
        val notifType = NotificationClassifier.classify(sbn)
        if (notifType == CapsuleNotificationType.CALL) {
            capsuleManager.dismissCallCapsule()
        }
    }

    override fun onDestroy() {
        if (instance == this) instance = null
        serviceScope.cancel()
        isListenerConnected = false
        super.onDestroy()
    }

    companion object {
        var isListenerConnected: Boolean = false
            private set
        var instance: MagicNotificationListenerService? = null
            private set

        fun ensureServiceBound(context: Context) {
            val cn = ComponentName(context, MagicNotificationListenerService::class.java)
            val pm = context.packageManager
            try {
                // Component re-toggle forces Android NotificationManagerService to reconnect stalled listeners
                pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            } catch (_: Exception) {}

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestRebind(cn)
                    Log.d("MagicCapsuleListener", "requestRebind sent for $cn")
                } catch (e: Exception) {
                    Log.w("MagicCapsuleListener", "requestRebind error: ${e.message}")
                }
            }
        }

        fun getNotificationChannelsForApp(packageName: String): List<NotificationChannel> {
            val service = instance ?: return emptyList()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    service.getNotificationChannels(packageName, android.os.Process.myUserHandle()) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            } else emptyList()
        }

        fun getNotificationChannelGroupsForApp(packageName: String): List<NotificationChannelGroup> {
            val service = instance ?: return emptyList()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    service.getNotificationChannelGroups(packageName, android.os.Process.myUserHandle()) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            } else emptyList()
        }
    }
}
