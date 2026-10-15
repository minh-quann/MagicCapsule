package com.buwin.magiccapsule.ui.components.toast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    PROGRESS
}

data class DownloadItemProgress(
    val downloadId: Long = -1L,
    val title: String = "",
    val message: String? = null,
    val progress: Int = -1,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val isAudio: Boolean = false
)

data class ToastState(
    val isVisible: Boolean = false,
    val type: ToastType = ToastType.INFO,
    val title: String = "",
    val message: String? = null,
    val progress: Int = -1, // 0..100, or -1 for indeterminate
    val durationMs: Long = 3500L,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val downloadId: Long = -1L,
    val items: List<DownloadItemProgress> = emptyList()
)

/**
 * Global singleton controller managing in-app Waterdrop dynamic toast states.
 * Mirrors the MEBIECO mobile WaterdropToast controller architecture.
 */
object AppToast {
    private val _state = MutableStateFlow(ToastState())
    val state: StateFlow<ToastState> = _state.asStateFlow()

    fun showProgress(
        title: String,
        message: String? = null,
        progress: Int = -1,
        downloadedBytes: Long = 0L,
        totalBytes: Long = 0L,
        speedBytesPerSec: Long = 0L,
        downloadId: Long = -1L,
        items: List<DownloadItemProgress> = emptyList()
    ) {
        val current = _state.value
        // Only generate a new timestamp when opening a new toast or transitioning from non-progress.
        // During ongoing download progress updates, keep the timestamp stable to prevent animation resets and UI flickering.
        val targetTimestamp = if (!current.isVisible || current.type != ToastType.PROGRESS) {
            System.currentTimeMillis()
        } else {
            current.timestamp
        }

        _state.value = ToastState(
            isVisible = true,
            type = ToastType.PROGRESS,
            title = title,
            message = message,
            progress = progress,
            durationMs = 0L, // Stays active while downloading
            timestamp = targetTimestamp,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            speedBytesPerSec = speedBytesPerSec,
            downloadId = downloadId,
            items = items
        )
    }

    fun showSuccess(
        title: String,
        message: String? = null,
        durationMs: Long = 4500L,
        downloadId: Long = -1L
    ) {
        _state.value = ToastState(
            isVisible = true,
            type = ToastType.SUCCESS,
            title = title,
            message = message,
            progress = 100,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            downloadId = downloadId
        )
    }

    fun showError(title: String, message: String? = null, durationMs: Long = 4000L) {
        _state.value = ToastState(
            isVisible = true,
            type = ToastType.ERROR,
            title = title,
            message = message,
            progress = -1,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis()
        )
    }

    fun showInfo(title: String, message: String? = null, durationMs: Long = 3000L) {
        _state.value = ToastState(
            isVisible = true,
            type = ToastType.INFO,
            title = title,
            message = message,
            progress = -1,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis()
        )
    }

    fun hide() {
        _state.value = _state.value.copy(isVisible = false)
    }

    /**
     * Helper to format raw byte count into human readable units (B, KB, MB, GB).
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    /**
     * Helper to format network transfer speed (KB/s, MB/s).
     */
    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 KB/s"
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
            else -> String.format(Locale.US, "%.0f KB/s", kb)
        }
    }
}
