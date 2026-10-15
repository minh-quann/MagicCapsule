package com.buwin.magiccapsule.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Central settings repository managing configuration for all Magic Capsule features.
 */
class AppFeaturePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("magic_capsule_features_prefs", Context.MODE_PRIVATE)

    // Feature Master Toggles
    var isPowerIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_POWER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_POWER_ENABLED, value).apply()

    var isDeviceIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DEVICE_ENABLED, value).apply()

    var isSmsIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_SMS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SMS_ENABLED, value).apply()

    var isLyricsIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_LYRICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_LYRICS_ENABLED, value).apply()

    var isNotificationIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ENABLED, value).apply()

    var isHeadsetIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_HEADSET_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HEADSET_ENABLED, value).apply()

    var isFaceUnlockIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_FACE_UNLOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_FACE_UNLOCK_ENABLED, value).apply()

    var isCalendarIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALENDAR_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CALENDAR_ENABLED, value).apply()

    var isApiIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_API_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_API_ENABLED, value).apply()

    var isDownloadIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOWNLOAD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DOWNLOAD_ENABLED, value).apply()

    var isScreenRecordIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREEN_RECORD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN_RECORD_ENABLED, value).apply()

    var isPinAppIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_APP_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_PIN_APP_ENABLED, value).apply()

    var isLoudnessIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOUDNESS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_LOUDNESS_ENABLED, value).apply()

    var isKeepAliveA11yEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEEP_ALIVE_A11Y, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_ALIVE_A11Y, value).apply()

    // Device Island Sub-settings
    var showRamMetric: Boolean
        get() = prefs.getBoolean(KEY_SHOW_RAM, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_RAM, value).apply()

    var showNetworkSpeedMetric: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NET_SPEED, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NET_SPEED, value).apply()

    var showTemperatureMetric: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DEVICE_TEMP, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DEVICE_TEMP, value).apply()

    var deviceIslandIntervalMs: Long
        get() = prefs.getLong(KEY_DEVICE_INTERVAL, 1500L)
        set(value) = prefs.edit().putLong(KEY_DEVICE_INTERVAL, value).apply()

    // SMS / OTP Sub-settings
    var autoCopyOtp: Boolean
        get() = prefs.getBoolean(KEY_AUTO_COPY_OTP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_COPY_OTP, value).apply()

    var smsCapsuleDurationSec: Int
        get() = prefs.getInt(KEY_SMS_DURATION, 12)
        set(value) = prefs.edit().putInt(KEY_SMS_DURATION, value).apply()

    // Headset Sub-settings
    var headsetDurationSec: Int
        get() = prefs.getInt(KEY_HEADSET_DURATION, 5)
        set(value) = prefs.edit().putInt(KEY_HEADSET_DURATION, value).apply()

    var showHeadsetBattery: Boolean
        get() = prefs.getBoolean(KEY_HEADSET_BATTERY, true)
        set(value) = prefs.edit().putBoolean(KEY_HEADSET_BATTERY, value).apply()

    // Notification Island Sub-settings
    var notificationDurationSec: Int
        get() = prefs.getInt(KEY_NOTIFY_DURATION, 6)
        set(value) = prefs.edit().putInt(KEY_NOTIFY_DURATION, value).apply()

    var filterImportantOnly: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_FILTER_IMPORTANT, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_FILTER_IMPORTANT, value).apply()

    var isHideOriginalNotification: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_HIDE_ORIGINAL, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_HIDE_ORIGINAL, value).apply()

    var isAllowCallCapsule: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ALLOW_CALL, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ALLOW_CALL, value).apply()

    var isAllowChatCapsule: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ALLOW_CHAT, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ALLOW_CHAT, value).apply()

    var isAllowOtherCapsule: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ALLOW_OTHER, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ALLOW_OTHER, value).apply()

    var notificationAllowedApps: Set<String>
        get() = prefs.getStringSet(KEY_NOTIFY_ALLOWED_APPS, setOf(
            "com.zing.zalo",
            "com.facebook.orca",
            "org.telegram.messenger",
            "com.whatsapp",
            "com.google.android.apps.messaging",
            "com.viber.voip",
            "com.google.android.gm"
        )) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_NOTIFY_ALLOWED_APPS, value).apply()

    var notificationHideBannerApps: Set<String>
        get() = prefs.getStringSet(KEY_NOTIFY_HIDE_BANNER_APPS, setOf(
            "com.zing.zalo",
            "com.facebook.orca",
            "org.telegram.messenger"
        )) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_NOTIFY_HIDE_BANNER_APPS, value).apply()

    fun isAppAllowedForCapsule(packageName: String): Boolean {
        return notificationAllowedApps.contains(packageName)
    }

    fun setAppAllowedForCapsule(packageName: String, allowed: Boolean) {
        val current = notificationAllowedApps.toMutableSet()
        if (allowed) current.add(packageName) else current.remove(packageName)
        notificationAllowedApps = current
    }

    fun isAppHideBanner(packageName: String): Boolean {
        return isHideOriginalNotification || notificationHideBannerApps.contains(packageName)
    }

    fun setAppHideBanner(packageName: String, hide: Boolean) {
        val current = notificationHideBannerApps.toMutableSet()
        if (hide) current.add(packageName) else current.remove(packageName)
        notificationHideBannerApps = current
    }

    fun isAppChannelAllowed(packageName: String, channelId: String): Boolean {
        if (channelId.isBlank()) return true
        val disabled = getDisabledChannelsForApp(packageName)
        if (disabled.isEmpty()) return true
        if (disabled.contains(channelId)) return false

        val chLower = channelId.lowercase()
        for (d in disabled) {
            val dLower = d.lowercase()
            if (chLower == dLower) return false
            if (dLower.contains("call") && chLower.contains("call")) return false
            if ((dLower.contains("chat") || dLower.contains("inapp")) && (chLower.contains("chat") || chLower.contains("msg") || chLower.contains("inapp"))) return false
            if (dLower.contains("interaction") && chLower.contains("interaction")) return false
            if (dLower.contains("activity") && chLower.contains("activity")) return false
            if (dLower.contains("quiet") && chLower.contains("quiet")) return false
            if (dLower.contains("coupon") && chLower.contains("coupon")) return false
        }
        return true
    }

    fun setAppChannelAllowed(packageName: String, channelId: String, allowed: Boolean) {
        val disabled = getDisabledChannelsForApp(packageName).toMutableSet()
        if (allowed) {
            disabled.remove(channelId)
        } else {
            disabled.add(channelId)
        }
        prefs.edit().putStringSet("disabled_channels_$packageName", disabled).apply()
    }

    fun getDisabledChannelsForApp(packageName: String): Set<String> {
        return prefs.getStringSet("disabled_channels_$packageName", emptySet()) ?: emptySet()
    }

    // Refresh rate & Island behavior
    var refreshRateSec: Int
        get() = prefs.getInt(KEY_REFRESH_RATE_SEC, 2)
        set(value) = prefs.edit().putInt(KEY_REFRESH_RATE_SEC, value).apply()

    var isAutoStartOnBoot: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START_BOOT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START_BOOT, value).apply()

    var isHideFromRecents: Boolean
        get() = prefs.getBoolean(KEY_HIDE_RECENTS, false)
        set(value) = prefs.edit().putBoolean(KEY_HIDE_RECENTS, value).apply()

    var isDeviceAlertEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEVICE_ALERT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DEVICE_ALERT_ENABLED, value).apply()

    var maxBatteryTempAlert: Float
        get() = prefs.getFloat(KEY_BATTERY_TEMP_ALERT, 42.0f)
        set(value) = prefs.edit().putFloat(KEY_BATTERY_TEMP_ALERT, value).apply()

    var isCustomCardPermanent: Boolean
        get() = prefs.getBoolean(KEY_CUSTOM_CARD_PERMANENT, false)
        set(value) = prefs.edit().putBoolean(KEY_CUSTOM_CARD_PERMANENT, value).apply()

    var customCardText: String
        get() = prefs.getString(KEY_CUSTOM_CARD_TEXT, "✨ Magic Capsule • Honor MagicOS") ?: "✨ Magic Capsule • Honor MagicOS"
        set(value) = prefs.edit().putString(KEY_CUSTOM_CARD_TEXT, value).apply()

    var isIslandScrollEnabled: Boolean
        get() = prefs.getBoolean(KEY_ISLAND_SCROLL_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ISLAND_SCROLL_ENABLED, value).apply()

    var isTurnOffOnScreenLock: Boolean
        get() = prefs.getBoolean(KEY_TURN_OFF_SCREEN_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_TURN_OFF_SCREEN_LOCK, value).apply()

    // API Island Sub-settings
    var apiIslandPort: Int
        get() = prefs.getInt(KEY_API_PORT, 8089)
        set(value) = prefs.edit().putInt(KEY_API_PORT, value).apply()

    var apiIslandToken: String
        get() = prefs.getString(KEY_API_TOKEN, "magic_honor_2026") ?: "magic_honor_2026"
        set(value) = prefs.edit().putString(KEY_API_TOKEN, value).apply()

    // Pinned App
    var pinnedPackage: String?
        get() = prefs.getString(KEY_PINNED_PACKAGE, null)
        set(value) = prefs.edit().putString(KEY_PINNED_PACKAGE, value).apply()

    var pinnedAppName: String?
        get() = prefs.getString(KEY_PINNED_APP_NAME, null)
        set(value) = prefs.edit().putString(KEY_PINNED_APP_NAME, value).apply()

    companion object {
        private const val KEY_POWER_ENABLED = "feat_power_enabled"
        private const val KEY_DEVICE_ENABLED = "feat_device_enabled"
        private const val KEY_SMS_ENABLED = "feat_sms_enabled"
        private const val KEY_LYRICS_ENABLED = "feat_lyrics_enabled"
        private const val KEY_NOTIFY_ENABLED = "feat_notify_enabled"
        private const val KEY_HEADSET_ENABLED = "feat_headset_enabled"
        private const val KEY_FACE_UNLOCK_ENABLED = "feat_face_unlock_enabled"
        private const val KEY_CALENDAR_ENABLED = "feat_calendar_enabled"
        private const val KEY_API_ENABLED = "feat_api_enabled"
        private const val KEY_DOWNLOAD_ENABLED = "feat_download_enabled"
        private const val KEY_SCREEN_RECORD_ENABLED = "feat_screen_record_enabled"
        private const val KEY_PIN_APP_ENABLED = "feat_pin_app_enabled"
        private const val KEY_LOUDNESS_ENABLED = "feat_loudness_enabled"
        private const val KEY_KEEP_ALIVE_A11Y = "feat_keep_alive_a11y"

        private const val KEY_SHOW_RAM = "dev_show_ram"
        private const val KEY_SHOW_NET_SPEED = "dev_show_net_speed"
        private const val KEY_SHOW_DEVICE_TEMP = "dev_show_temp"
        private const val KEY_DEVICE_INTERVAL = "dev_interval_ms"

        private const val KEY_AUTO_COPY_OTP = "sms_auto_copy_otp"
        private const val KEY_SMS_DURATION = "sms_duration_sec"

        private const val KEY_HEADSET_DURATION = "headset_duration_sec"
        private const val KEY_HEADSET_BATTERY = "headset_battery"

        private const val KEY_NOTIFY_DURATION = "notify_duration_sec"
        private const val KEY_NOTIFY_FILTER_IMPORTANT = "notify_filter_important"
        private const val KEY_NOTIFY_HIDE_ORIGINAL = "notify_hide_original"
        private const val KEY_NOTIFY_ALLOWED_APPS = "notify_allowed_apps"
        private const val KEY_NOTIFY_HIDE_BANNER_APPS = "notify_hide_banner_apps"
        private const val KEY_NOTIFY_ALLOW_CALL = "notify_allow_call"
        private const val KEY_NOTIFY_ALLOW_CHAT = "notify_allow_chat"
        private const val KEY_NOTIFY_ALLOW_OTHER = "notify_allow_other"

        private const val KEY_REFRESH_RATE_SEC = "refresh_rate_sec"
        private const val KEY_AUTO_START_BOOT = "auto_start_boot"
        private const val KEY_HIDE_RECENTS = "hide_from_recents"
        private const val KEY_DEVICE_ALERT_ENABLED = "device_alert_enabled"
        private const val KEY_BATTERY_TEMP_ALERT = "battery_temp_alert"
        private const val KEY_CUSTOM_CARD_PERMANENT = "custom_card_permanent"
        private const val KEY_CUSTOM_CARD_TEXT = "custom_card_text"
        private const val KEY_ISLAND_SCROLL_ENABLED = "island_scroll_enabled"
        private const val KEY_TURN_OFF_SCREEN_LOCK = "turn_off_screen_lock"

        private const val KEY_API_PORT = "api_island_port"
        private const val KEY_API_TOKEN = "api_island_token"

        private const val KEY_PINNED_PACKAGE = "pinned_package"
        private const val KEY_PINNED_APP_NAME = "pinned_app_name"
    }
}
