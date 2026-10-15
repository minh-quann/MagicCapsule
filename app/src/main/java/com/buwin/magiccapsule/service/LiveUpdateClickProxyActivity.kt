package com.buwin.magiccapsule.service

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.buwin.magiccapsule.capsule.CapsuleManager

/**
 * Transparent proxy activity bridging capsule clicks to target apps (Zalo, Messenger, Spotify, etc.)
 * Bypasses Android 14+ / MagicOS Background Activity Launch (BAL) restrictions so clicking
 * the capsule always launches the originating third-party application instead of MagicCapsule.
 */
class LiveUpdateClickProxyActivity : Activity() {

    companion object {
        const val EXTRA_SOURCE_PACKAGE = "source_package"
        const val EXTRA_ORIGINAL_PI = "original_pi"
        const val EXTRA_ORIGINAL_INTENT = "original_intent"
        const val EXTRA_SOURCE_KEY = "source_key"
        const val EXTRA_CAPSULE_ID = "capsule_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish()
        applyZeroTransition()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        finish()
        applyZeroTransition()
    }

    private fun applyZeroTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val sourcePackage = intent.getStringExtra(EXTRA_SOURCE_PACKAGE)
        val capsuleId = intent.getIntExtra(EXTRA_CAPSULE_ID, CapsuleManager.ID_NOTIFY)

        // 1. Dismiss the capsule immediately upon tap
        try {
            val capsuleManager = CapsuleManager(this)
            capsuleManager.dismiss(capsuleId)
        } catch (_: Exception) {}

        @Suppress("DEPRECATION")
        val originalPi: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_ORIGINAL_PI, PendingIntent::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_ORIGINAL_PI)
        }

        @Suppress("DEPRECATION")
        val originalIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_ORIGINAL_INTENT, Intent::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_ORIGINAL_INTENT)
        }

        var launched = false

        // 2. Try launching original PendingIntent with Android 14+ BAL permission
        if (originalPi != null) {
            try {
                val options = ActivityOptions.makeBasic()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    @Suppress("DEPRECATION")
                    options.setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                }
                originalPi.send(this, 0, null, null, null, null, options.toBundle())
                launched = true
            } catch (_: Exception) {}
        }

        // 3. Fallback: launch original Intent if provided
        if (!launched && originalIntent != null) {
            try {
                originalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(originalIntent)
                launched = true
            } catch (_: Exception) {}
        }

        // 4. Fallback: Launch the target application by its package name (Zalo, Messenger, etc.)
        if (!launched && !sourcePackage.isNullOrBlank()) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(sourcePackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(launchIntent)
                    launched = true
                }
            } catch (_: Exception) {}
        }
    }
}
