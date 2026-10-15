package com.buwin.magiccapsule.data.notification

import android.app.Notification
import android.os.Build

/**
 * Robust notification extractor retrieving real message text and sender information
 * across all Android notification styles (MessagingStyle, BigTextStyle, InboxStyle, standard).
 */
object NotificationExtractor {

    fun extractTitle(notification: Notification): String {
        val extras = notification.extras ?: return ""

        val convTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        if (!convTitle.isNullOrBlank()) return convTitle.trim()

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        if (!title.isNullOrBlank()) return title.trim()

        val bigTitle = extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
        if (!bigTitle.isNullOrBlank()) return bigTitle.trim()

        return ""
    }

    fun extractText(notification: Notification): String {
        val extras = notification.extras ?: return ""

        // 1. MessagingStyle (used by Zalo, WhatsApp, Messenger, Telegram)
        try {
            val messagesArray = extras.getParcelableArray("android.messages")
            if (messagesArray != null && messagesArray.isNotEmpty()) {
                val messages = Notification.MessagingStyle.Message.getMessagesFromBundleArray(messagesArray)
                val lastMsg = messages.lastOrNull { !it.text.isNullOrBlank() }
                if (lastMsg != null) {
                    val sender = lastMsg.senderPerson?.name ?: ""
                    val content = lastMsg.text.toString().trim()
                    return if (sender.isNotBlank()) "$sender: $content" else content
                }
            }
        } catch (_: Exception) {}

        // 2. Big text (BigTextStyle)
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        if (!bigText.isNullOrBlank()) return bigText.trim()

        // 3. Text lines (InboxStyle)
        try {
            val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            if (lines != null && lines.isNotEmpty()) {
                val lastLine = lines.lastOrNull { !it.isNullOrBlank() }
                if (lastLine != null) return lastLine.toString().trim()
            }
        } catch (_: Exception) {}

        // 4. Standard notification text
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        if (!text.isNullOrBlank()) return text.trim()

        // 5. SubText / InfoText
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        if (!subText.isNullOrBlank()) return subText.trim()

        return ""
    }
}
