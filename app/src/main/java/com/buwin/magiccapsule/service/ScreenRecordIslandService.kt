package com.buwin.magiccapsule.service

import android.content.Context
import android.os.SystemClock
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Screen recording island detecting Honor / Huawei screen recorder
 * and displaying a live recording timer on the Magic Capsule.
 */
object ScreenRecordIslandService {

    private val RECORD_PACKAGES = setOf(
        "com.hihonor.screenrecorder",
        "com.huawei.screenrecorder",
        "com.android.systemui.screenrecord",
        "com.miui.screenrecorder",
        "com.oplus.screenrecorder"
    )

    private val scope = CoroutineScope(Dispatchers.Default)
    private var tickerJob: Job? = null
    private var recordStartedAt: Long = 0L

    fun isScreenRecorderPackage(packageName: String): Boolean {
        return RECORD_PACKAGES.contains(packageName)
    }

    fun onRecordStarted(context: Context) {
        val prefs = AppFeaturePreferences(context)
        if (!prefs.isScreenRecordIslandEnabled) return

        if (tickerJob?.isActive == true) return

        recordStartedAt = SystemClock.elapsedRealtime()
        val capsuleManager = CapsuleManager(context)

        tickerJob = scope.launch {
            while (isActive) {
                val elapsedSec = (SystemClock.elapsedRealtime() - recordStartedAt) / 1000L
                val timeStr = String.format(Locale.US, "%02d:%02d", elapsedSec / 60, elapsedSec % 60)
                capsuleManager.postScreenRecordCapsule(timeStr)
                delay(1000L)
            }
        }
    }

    fun onRecordStopped(context: Context) {
        tickerJob?.cancel()
        tickerJob = null
        val capsuleManager = CapsuleManager(context)
        capsuleManager.dismiss(CapsuleManager.ID_SCREEN_RECORD)
    }
}
