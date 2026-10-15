package com.buwin.magiccapsule.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Real-time environmental sound pressure (decibel) monitor island.
 */
class LoudnessMonitorService : Service() {

    private lateinit var capsuleManager: CapsuleManager
    private lateinit var prefs: AppFeaturePreferences
    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    override fun onCreate() {
        super.onCreate()
        ForegroundHelper.startForegroundServiceCompat(
            service = this,
            notificationId = 3004,
            title = "Đo độ ồn",
            foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
        capsuleManager = CapsuleManager(this)
        prefs = AppFeaturePreferences(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!prefs.isLoudnessIslandEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        recordJob?.cancel()
        recordJob = scope.launch {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(1024)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize
                )
                audioRecord?.startRecording()

                val buffer = ShortArray(minBufferSize)
                while (isActive) {
                    val read = audioRecord?.read(buffer, 0, minBufferSize) ?: 0
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / read)
                        val db = if (rms > 0) (20 * log10(rms)).toInt() else 0
                        val calibratedDb = (db + 20).coerceIn(20, 120) // Scale to human-perceptible range

                        capsuleManager.postLoudnessCapsule(calibratedDb)
                    }
                    delay(800L)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                audioRecord = null
            }
        }
    }

    override fun onDestroy() {
        recordJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        capsuleManager.dismiss(CapsuleManager.ID_LOUDNESS)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoudnessMonitorService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LoudnessMonitorService::class.java))
        }
    }
}
