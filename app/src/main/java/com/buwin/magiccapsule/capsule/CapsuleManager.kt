package com.buwin.magiccapsule.capsule

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import com.buwin.magiccapsule.MainActivity
import com.buwin.magiccapsule.R
import com.buwin.magiccapsule.receiver.OtpCopyReceiver
import com.buwin.magiccapsule.service.LiveUpdateClickProxyActivity

/**
 * Master dispatcher formatting and publishing notifications according to the Honor MagicOS
 * Magic Capsule (灵动胶囊) protocol.
 */
class CapsuleManager(private val context: Context) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        const val CHANNEL_CHARGING = "magic_charging_v5"
        const val CHANNEL_DEVICE = "magic_device_v5"
        const val CHANNEL_SMS = "magic_sms_v5"
        const val CHANNEL_MEDIA = "magic_media_v5"
        const val CHANNEL_NOTIFY = "magic_notify_v5"
        const val CHANNEL_HEADSET = "magic_headset_v5"
        const val CHANNEL_API = "magic_api_v5"
        const val CHANNEL_DOWNLOAD = "magic_download_v5"
        const val CHANNEL_SCREEN_RECORD = "magic_screen_record_v5"
        const val CHANNEL_CALENDAR = "magic_calendar_v5"
        const val CHANNEL_FACE_UNLOCK = "magic_face_unlock_v5"
        const val CHANNEL_PIN_APP = "magic_pin_app_v5"
        const val CHANNEL_LOUDNESS = "magic_loudness_v5"
        const val CHANNEL_CALL = "magic_call_v5"

        const val ID_CHARGING = 2026
        const val ID_DEVICE = 2027
        const val ID_SMS = 2028
        const val ID_MEDIA = 2029
        const val ID_NOTIFY = 2030
        const val ID_HEADSET = 2031
        const val ID_API = 2032
        const val ID_DOWNLOAD = 2033
        const val ID_SCREEN_RECORD = 2034
        const val ID_CALENDAR = 2035
        const val ID_FACE_UNLOCK = 2036
        const val ID_PIN_APP = 2037
        const val ID_LOUDNESS = 2038
        const val ID_CALL = 2039
    }

    init {
        createAllChannels()
    }

    private fun createAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete legacy channels that were permanently stuck at IMPORTANCE_DEFAULT (3)
            val legacyChannels = listOf(
                "capsule_charging", "capsule_device", "capsule_sms", "capsule_media",
                "capsule_notify", "capsule_headset", "capsule_api", "capsule_download",
                "capsule_screen_record", "capsule_calendar", "capsule_face_unlock",
                "capsule_pin_app", "capsule_loudness"
            )
            legacyChannels.forEach { oldId ->
                try {
                    notificationManager.deleteNotificationChannel(oldId)
                } catch (_: Exception) {}
            }

            val channels = listOf(
                NotificationChannel(CHANNEL_CHARGING, "Sạc pin & Công suất", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_DEVICE, "Thông số thiết bị (RAM/Mạng)", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_SMS, "Mã xác minh & Bưu kiện", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_MEDIA, "Trình phát nhạc & Lời bài hát", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_NOTIFY, "Thông báo ứng dụng", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_CALL, "Cuộc gọi thoại & Video", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_HEADSET, "Tai nghe Bluetooth", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_API, "API & Webhook", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_DOWNLOAD, "Tiến độ tải xuống", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_SCREEN_RECORD, "Ghi màn hình", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_CALENDAR, "Lịch & Sự kiện", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_FACE_UNLOCK, "Mở khóa khuôn mặt", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_PIN_APP, "Ghim ứng dụng & Trở về nhanh", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_LOUDNESS, "Đo độ ồn (Decibel)", NotificationManager.IMPORTANCE_HIGH)
            )

            channels.forEach { channel ->
                // Honor MagicOS requires IMPORTANCE_HIGH to promote notification to Magic Capsule pill
                channel.setSound(null, null)
                channel.enableVibration(false)
                channel.setShowBadge(false)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Publishes charging wattage capsule.
     */
    fun postChargingCapsule(capsuleText: String, title: String, details: String) {
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_CHARGING,
            capsuleText = capsuleText,
            title = title,
            details = details,
            iconRes = R.drawable.ic_charging,
            isOngoing = true
        )
        notificationManager.notify(ID_CHARGING, notification)
    }

    /**
     * Publishes a device hardware stats capsule (RAM & Network speed).
     */
    fun postDeviceStatsCapsule(capsuleText: String, title: String, details: String) {
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_DEVICE,
            capsuleText = capsuleText,
            title = title,
            details = details,
            iconRes = R.drawable.ic_ram,
            isOngoing = true
        )
        notificationManager.notify(ID_DEVICE, notification)
    }

    /**
     * Publishes a temporary SMS OTP capsule with a 1-tap copy action button.
     */
    fun postSmsOtpCapsule(code: String, typeLabel: String, sender: String) {
        val capsuleTitle = "🔑 $code"
        val titleText = "$typeLabel: $code"
        val details = "Từ: $sender • Nhấn để sao chép mã ngay"

        val copyIntent = Intent(context, OtpCopyReceiver::class.java).apply {
            action = OtpCopyReceiver.ACTION_COPY_OTP
            putExtra(OtpCopyReceiver.EXTRA_CODE, code)
        }
        val pendingCopy = PendingIntent.getBroadcast(
            context,
            code.hashCode(),
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = buildCapsuleNotification(
            channelId = CHANNEL_SMS,
            capsuleText = capsuleTitle,
            title = titleText,
            details = details,
            iconRes = R.drawable.ic_sms,
            contentIntent = pendingCopy,
            isOngoing = true
        )
        notificationManager.notify(ID_SMS, notification)
    }

    /**
     * Creates a proxy PendingIntent routing through LiveUpdateClickProxyActivity
     * to safely open third-party apps (Zalo, Messenger, Spotify, etc.) bypassing Android BAL rules.
     */
    fun createProxyPendingIntent(
        packageName: String,
        originalPi: PendingIntent? = null,
        capsuleId: Int = ID_NOTIFY,
        sourceKey: String? = null
    ): PendingIntent {
        val intent = Intent(context, LiveUpdateClickProxyActivity::class.java).apply {
            putExtra(LiveUpdateClickProxyActivity.EXTRA_SOURCE_PACKAGE, packageName)
            putExtra(LiveUpdateClickProxyActivity.EXTRA_ORIGINAL_PI, originalPi)
            putExtra(LiveUpdateClickProxyActivity.EXTRA_SOURCE_KEY, sourceKey)
            putExtra(LiveUpdateClickProxyActivity.EXTRA_CAPSULE_ID, capsuleId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(
            context,
            (packageName.hashCode() xor capsuleId),
            intent,
            flags
        )
    }

    /**
     * Publishes media playback capsule (track title & artist).
     */
    fun postMediaCapsule(
        capsuleText: String,
        title: String,
        artist: String,
        packageName: String? = null,
        originalContentIntent: PendingIntent? = null
    ) {
        val targetPi = if (!packageName.isNullOrBlank()) {
            createProxyPendingIntent(packageName, originalContentIntent, ID_MEDIA)
        } else null

        val notification = buildCapsuleNotification(
            channelId = CHANNEL_MEDIA,
            capsuleText = capsuleText,
            title = "🎵 $title",
            details = artist,
            iconRes = R.drawable.ic_notification,
            contentIntent = targetPi,
            isOngoing = true
        )
        notificationManager.notify(ID_MEDIA, notification)
    }

    /**
     * Publishes Bluetooth headset connection status with battery.
     */
    fun postHeadsetCapsule(deviceName: String, batteryText: String) {
        val capsuleTitle = "🎧 $deviceName $batteryText".trim()
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_HEADSET,
            capsuleText = capsuleTitle,
            title = "Đã kết nối $deviceName",
            details = if (batteryText.isNotBlank()) "Mức pin: $batteryText" else "Sẵn sàng phát âm thanh",
            iconRes = R.drawable.ic_headset,
            isOngoing = true
        )
        notificationManager.notify(ID_HEADSET, notification)
    }

    /**
     * Publishes app notification mirror capsule.
     * When user clicks the capsule, it launches the target app directly (Zalo, Messenger, etc.)
     */
    fun postNotificationCapsule(
        packageName: String,
        appName: String,
        title: String,
        text: String,
        originalContentIntent: PendingIntent? = null,
        sourceKey: String? = null
    ) {
        val displayMsg = text.ifBlank { title }
        val capsuleTitle = "💬 $appName: $displayMsg".take(24)
        val targetPi = createProxyPendingIntent(
            packageName = packageName,
            originalPi = originalContentIntent,
            capsuleId = ID_NOTIFY,
            sourceKey = sourceKey
        )
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_NOTIFY,
            capsuleText = capsuleTitle,
            title = if (title.isNotBlank()) "$appName: $title" else appName,
            details = displayMsg,
            iconRes = R.drawable.ic_notification,
            contentIntent = targetPi,
            isOngoing = true
        )
        notificationManager.notify(ID_NOTIFY, notification)
    }

    /**
     * Publishes incoming or active VoIP call capsule (Zalo call, Messenger call, etc.)
     */
    fun postCallCapsule(
        packageName: String,
        appName: String,
        callerName: String,
        callStatus: String = "Cuộc gọi đến",
        originalContentIntent: PendingIntent? = null,
        sourceKey: String? = null
    ) {
        val capsuleTitle = "📞 $appName: $callerName".take(24)
        val targetPi = createProxyPendingIntent(
            packageName = packageName,
            originalPi = originalContentIntent,
            capsuleId = ID_CALL,
            sourceKey = sourceKey
        )
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_CALL,
            capsuleText = capsuleTitle,
            title = "📞 $appName: $callerName",
            details = callStatus,
            iconRes = R.drawable.ic_notification,
            contentIntent = targetPi,
            isOngoing = true
        )
        notificationManager.notify(ID_CALL, notification)
    }

    fun dismissCallCapsule() {
        notificationManager.cancel(ID_CALL)
    }

    /**
     * Publishes universal download progress capsule.
     */
    fun postDownloadCapsule(progress: Int, title: String, speedText: String) {
        val capsuleTitle = "⬇️ $progress% $title".take(22)
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_DOWNLOAD,
            capsuleText = capsuleTitle,
            title = "Đang tải: $title ($progress%)",
            details = speedText,
            iconRes = R.drawable.ic_download,
            isOngoing = true
        )
        notificationManager.notify(ID_DOWNLOAD, notification)
    }

    /**
     * Publishes screen recording timer capsule.
     */
    fun postScreenRecordCapsule(elapsedText: String) {
        val capsuleTitle = "🔴 $elapsedText"
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_SCREEN_RECORD,
            capsuleText = capsuleTitle,
            title = "Đang ghi màn hình",
            details = "Thời gian đã ghi: $elapsedText",
            iconRes = R.drawable.ic_timer,
            isOngoing = true
        )
        notificationManager.notify(ID_SCREEN_RECORD, notification)
    }

    /**
     * Publishes upcoming calendar event countdown capsule.
     */
    fun postCalendarCapsule(title: String, countdownText: String) {
        val capsuleTitle = "📅 $countdownText: $title".take(22)
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_CALENDAR,
            capsuleText = capsuleTitle,
            title = "Sắp diễn ra: $title",
            details = "Thời gian: $countdownText",
            iconRes = R.drawable.ic_calendar,
            isOngoing = true
        )
        notificationManager.notify(ID_CALENDAR, notification)
    }

    /**
     * Publishes face unlock state capsule.
     */
    fun postFaceUnlockCapsule(isUnlocked: Boolean) {
        val capsuleTitle = if (isUnlocked) "🔓 Đã mở khóa" else "🔒 Đang khóa"
        val iconRes = if (isUnlocked) R.drawable.ic_lock_open else R.drawable.ic_lock
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_FACE_UNLOCK,
            capsuleText = capsuleTitle,
            title = if (isUnlocked) "Mở khóa khuôn mặt thành công" else "Thiết bị đang khóa",
            details = "Magic Capsule Face ID",
            iconRes = iconRes,
            isOngoing = false
        )
        notificationManager.notify(ID_FACE_UNLOCK, notification)
    }

    /**
     * Publishes pinned app capsule for instant return.
     */
    fun postPinAppCapsule(appName: String, launchIntent: PendingIntent?) {
        val capsuleTitle = "📌 $appName"
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_PIN_APP,
            capsuleText = capsuleTitle,
            title = "Đã ghim: $appName",
            details = "Chạm vào Magic Capsule để quay lại ứng dụng ngay lập tức",
            iconRes = R.drawable.ic_pin,
            contentIntent = launchIntent,
            isOngoing = true
        )
        notificationManager.notify(ID_PIN_APP, notification)
    }

    /**
     * Publishes real-time decibel / ambient loudness capsule.
     */
    fun postLoudnessCapsule(decibel: Int) {
        val capsuleTitle = "🔊 $decibel dB"
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_LOUDNESS,
            capsuleText = capsuleTitle,
            title = "Độ ồn môi trường: $decibel dB",
            details = when {
                decibel > 80 -> "Cảnh báo: Tiếng ồn rất lớn, có thể hại thính giác"
                decibel > 65 -> "Môi trường tương đối ồn ào"
                decibel > 40 -> "Độ ồn trung bình (văn phòng / trò chuyện)"
                else -> "Môi trường rất yên tĩnh"
            },
            iconRes = R.drawable.ic_volume,
            isOngoing = true
        )
        notificationManager.notify(ID_LOUDNESS, notification)
    }

    /**
     * Publishes API & Webhook live update capsule.
     */
    fun postApiCapsule(capsuleText: String, title: String, details: String) {
        val notification = buildCapsuleNotification(
            channelId = CHANNEL_API,
            capsuleText = capsuleText,
            title = title,
            details = details,
            iconRes = R.drawable.ic_api,
            isOngoing = true
        )
        notificationManager.notify(ID_API, notification)
    }

    /**
     * Generic builder applying Honor MagicOS Magic Capsule extras.
     */
    private fun buildCapsuleNotification(
        channelId: String,
        capsuleText: String,
        title: String,
        details: String,
        iconRes: Int = R.drawable.ic_charging,
        largeIcon: Bitmap? = null,
        contentIntent: PendingIntent? = null,
        isOngoing: Boolean = true
    ): Notification {
        val targetIntent = contentIntent ?: run {
            val openApp = Intent(context, MainActivity::class.java)
            PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val capsuleExtras = Bundle().apply {
            putBoolean("android.requestPromotedOngoing", true)
            putString("android.title.capsule", capsuleText)
            putString("capsule_display_mode", "1")
        }

        val category = when (channelId) {
            CHANNEL_CHARGING, CHANNEL_DEVICE, CHANNEL_DOWNLOAD, CHANNEL_SCREEN_RECORD -> Notification.CATEGORY_PROGRESS
            CHANNEL_CALL -> Notification.CATEGORY_CALL
            else -> "msg"
        }

        val builder = Notification.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(details)
            .setContentIntent(targetIntent)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setTicker(capsuleText)
            .setCategory(category)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setStyle(Notification.BigTextStyle().bigText(details))
            .addExtras(capsuleExtras)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        if (Build.VERSION.SDK_INT >= 36) {
            // Android 16/17 (API 36+) Promoted Ongoing status bar chip / Magic Capsule text
            try {
                val method = Notification.Builder::class.java.getMethod("setShortCriticalText", CharSequence::class.java)
                method.invoke(builder, capsuleText)
            } catch (_: Exception) {
                try {
                    val method = Notification.Builder::class.java.getMethod("setShortCriticalText", String::class.java)
                    method.invoke(builder, capsuleText)
                } catch (_: Exception) {}
            }

            try {
                val method = Notification.Builder::class.java.getMethod("setRequestPromotedOngoing", Boolean::class.javaPrimitiveType)
                method.invoke(builder, true)
            } catch (_: Exception) {}
        }

        return builder.build()
    }

    fun dismiss(id: Int) {
        notificationManager.cancel(id)
    }
}
