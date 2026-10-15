package com.buwin.magiccapsule.data.notification

import android.app.Notification
import android.service.notification.StatusBarNotification

/**
 * Categorizes notifications into Call, Chat, or Other
 * to allow granular filtering per user preferences.
 */
enum class CapsuleNotificationType(val label: String) {
    CALL("Cuộc gọi"),
    CHAT("Tin nhắn"),
    OTHER("Khác")
}

object NotificationClassifier {

    /**
     * Inspects notification category, channel IDs, styles, actions, and extras
     * to accurately detect VoIP calls (Zalo, Messenger, Telegram), chat messages, or other notifications.
     */
    fun classify(sbn: StatusBarNotification): CapsuleNotificationType {
        val notification = sbn.notification ?: return CapsuleNotificationType.OTHER
        val category = notification.category ?: ""
        val channelId = (notification.channelId ?: "").lowercase()
        val extras = notification.extras

        // 1. CALL DETECTION (VoIP incoming / ongoing calls)
        if (category == Notification.CATEGORY_CALL ||
            category == "call" ||
            channelId.contains("call") ||
            channelId.contains("voip") ||
            channelId.contains("ringing") ||
            channelId.contains("cuoc_goi") ||
            channelId.contains("incoming") ||
            isCallFromExtras(extras) ||
            isCallFromActions(notification)) {
            return CapsuleNotificationType.CALL
        }

        // 2. CHAT DETECTION (Conversations, groups, personal messages)
        val pkg = (sbn.packageName ?: "").lowercase()
        val isMessagingApp = pkg.contains("zalo") || pkg.contains("orca") || pkg.contains("telegram") ||
                pkg.contains("whatsapp") || pkg.contains("messages") || pkg.contains("viber")

        if (category == Notification.CATEGORY_MESSAGE ||
            category == "msg" ||
            channelId.contains("chat") ||
            channelId.contains("message") ||
            channelId.contains("tin_nhan") ||
            channelId.contains("conversation") ||
            isChatFromExtras(extras) ||
            isChatFromActions(notification) ||
            (isMessagingApp && !channelId.contains("interaction") && !channelId.contains("activity") && !channelId.contains("quiet"))) {
            return CapsuleNotificationType.CHAT
        }

        return CapsuleNotificationType.OTHER
    }

    private fun isCallFromExtras(extras: android.os.Bundle?): Boolean {
        if (extras == null) return false
        if (extras.containsKey(Notification.EXTRA_CALL_TYPE) ||
            extras.containsKey("android.callType") ||
            extras.containsKey("android.callPerson")) {
            return true
        }
        val template = extras.getString(Notification.EXTRA_TEMPLATE) ?: ""
        if (template.contains("CallStyle", ignoreCase = true)) {
            return true
        }
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase() ?: ""
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.lowercase() ?: ""
        return title.contains("cuộc gọi") || text.contains("cuộc gọi") ||
                title.contains("incoming call") || text.contains("incoming call") ||
                text.contains("đang gọi") || title.contains("đang gọi")
    }

    private fun isCallFromActions(notification: Notification): Boolean {
        val actions = notification.actions ?: return false
        var hasAccept = false
        var hasDecline = false
        for (action in actions) {
            val title = action.title?.toString()?.lowercase() ?: ""
            if (title.contains("trả lời") || title.contains("nghe") || title.contains("answer") || title.contains("chấp nhận")) {
                hasAccept = true
            }
            if (title.contains("từ chối") || title.contains("bỏ qua") || title.contains("decline") || title.contains("hang up")) {
                hasDecline = true
            }
        }
        return hasAccept || (hasAccept && hasDecline)
    }

    private fun isChatFromExtras(extras: android.os.Bundle?): Boolean {
        if (extras == null) return false
        if (extras.containsKey("android.messages") ||
            extras.containsKey(Notification.EXTRA_MESSAGES) ||
            extras.containsKey(Notification.EXTRA_CONVERSATION_TITLE) ||
            extras.containsKey(Notification.EXTRA_MESSAGING_PERSON)) {
            return true
        }
        val template = extras.getString(Notification.EXTRA_TEMPLATE) ?: ""
        return template.contains("MessagingStyle", ignoreCase = true)
    }

    private fun isChatFromActions(notification: Notification): Boolean {
        val actions = notification.actions ?: return false
        for (action in actions) {
            if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) {
                return true
            }
            val title = action.title?.toString()?.lowercase() ?: ""
            if (title.contains("trả lời") || title.contains("reply")) {
                return true
            }
        }
        return false
    }
}
