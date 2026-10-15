package com.buwin.magiccapsule.receiver

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.data.sms.SmsOtpParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Standard Telephony SMS receiver catching incoming SMS messages directly.
 * Parses OTP verification codes and parcel locker pickup codes.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = AppFeaturePreferences(context)
        if (!prefs.isSmsIslandEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: "SMS"
        val bodyBuilder = StringBuilder()
        for (msg in messages) {
            bodyBuilder.append(msg.displayMessageBody ?: "")
        }
        val fullBody = bodyBuilder.toString()
        if (fullBody.isBlank()) return

        val parsed = SmsOtpParser.parse(sender, fullBody) ?: return

        // Auto copy to clipboard if preferred
        if (prefs.autoCopyOtp) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("OTP Code", parsed.code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Đã tự động sao chép mã: ${parsed.code}", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
        }

        // Post to Magic Capsule
        val capsuleManager = CapsuleManager(context)
        capsuleManager.postSmsOtpCapsule(
            code = parsed.code,
            typeLabel = parsed.type.label,
            sender = parsed.sender
        )

        // Auto dismiss after configured seconds
        CoroutineScope(Dispatchers.Default).launch {
            delay(prefs.smsCapsuleDurationSec * 1000L)
            capsuleManager.dismiss(CapsuleManager.ID_SMS)
        }
    }
}
