package com.buwin.magiccapsule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val pkgName = sbn.packageName ?: ""

        // Ignore our own notifications
        if (pkgName == packageName) return

        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Extract deep message structure (MessagingStyle, BigTextStyle, InboxStyle for Zalo, Messenger, etc.)
        val extractedTitle = NotificationExtractor.extractTitle(notification)
        val extractedText = NotificationExtractor.extractText(notification)

        val title = extractedTitle.ifBlank { rawTitle }
        val text = extractedText.ifBlank { rawText }

        Log.d("MagicCapsuleListener", "Intercepted: pkg=$pkgName | title='$title' | text='$text'")

        // 1. Honor / Huawei Screen Recorder detection
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

        // 2. Check for SMS OTP or Parcel delivery codes
        if (prefs.isSmsIslandEnabled && (title.isNotBlank() || text.isNotBlank())) {
            val combined = "$title $text"
            val otpResult = SmsOtpParser.parse(title.ifBlank { pkgName }, combined)
            if (otpResult != null) {
                capsuleManager.postSmsOtpCapsule(
                    code = otpResult.code,
                    typeLabel = otpResult.type.label,
                    sender = otpResult.sender
                )
                serviceScope.launch {
                    delay(prefs.smsCapsuleDurationSec * 1000L)
                    capsuleManager.dismiss(CapsuleManager.ID_SMS)
                }
                return
            }
        }

        // 3. Check for Media Playback (Spotify, YouTube Music, Zing MP3, Apple Music...)
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

        // 4. Smart App Notification & Call Mirroring (Zalo, Messenger, Telegram, WhatsApp...)
        if (prefs.isNotificationIslandEnabled && (title.isNotBlank() || text.isNotBlank())) {
            // Ignore blank group summary notifications
            if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0 && title.isBlank() && text.isBlank()) {
                return
            }

            val isAllowed = prefs.isAppAllowedForCapsule(pkgName)
            if (isAllowed) {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkgName.substringAfterLast('.')
                }

                val notifType = NotificationClassifier.classify(sbn)
                val channelId = notification.channelId ?: ""

                // Dynamic Per-app real channel filter
                if (channelId.isNotBlank() && !prefs.isAppChannelAllowed(pkgName, channelId)) {
                    Log.d("MagicCapsuleListener", "Dropped by per-app channel filter: $channelId for $pkgName")
                    return
                }

                // Dynamically record channel name & ID into repository cache
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channelId.isNotBlank()) {
                    try {
                        val channels = getNotificationChannels(pkgName, android.os.Process.myUserHandle())
                        val ch = channels?.firstOrNull { it.id == channelId }
                        NotificationChannelRepository.recordSeenChannel(this, pkgName, channelId, ch?.name?.toString())
                    } catch (_: Exception) {}
                }



                if (notifType == CapsuleNotificationType.CALL) {
                    capsuleManager.postCallCapsule(
                        packageName = pkgName,
                        appName = appLabel,
                        callerName = title.ifBlank { text }.ifBlank { "Cuộc gọi đến" },
                        callStatus = text.ifBlank { "Đang đổ chuông..." },
                        originalContentIntent = notification.contentIntent,
                        sourceKey = sbn.key
                    )
                    return
                }

                // Chat or Other notification
                capsuleManager.postNotificationCapsule(
                    packageName = pkgName,
                    appName = appLabel,
                    title = title,
                    text = text,
                    originalContentIntent = notification.contentIntent,
                    sourceKey = sbn.key
                )

                // If configured to show ONLY Magic Capsule (hide system heads-up popup banner)
                if (prefs.isAppHideBanner(pkgName)) {
                    try {
                        cancelNotification(sbn.key)
                    } catch (_: Exception) {}

                    // Delayed re-cancel to eliminate secondary system banner re-posts
                    serviceScope.launch {
                        delay(350)
                        try {
                            cancelNotification(sbn.key)
                        } catch (_: Exception) {}
                    }
                }

                serviceScope.launch {
                    delay(prefs.notificationDurationSec * 1000L)
                    capsuleManager.dismiss(CapsuleManager.ID_NOTIFY)
                }
            }
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
