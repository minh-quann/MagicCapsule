package com.buwin.magiccapsule.receiver

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Handles quick tap-to-copy action from Magic Capsule OTP pills.
 */
class OtpCopyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val code = intent.getStringExtra(EXTRA_CODE) ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Magic Capsule OTP", code)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "Đã sao chép: $code", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_COPY_OTP = "com.buwin.magiccapsule.action.COPY_OTP"
        const val EXTRA_CODE = "extra_otp_code"
    }
}
