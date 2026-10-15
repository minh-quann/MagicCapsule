package com.buwin.magiccapsule.service

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Universal Download Island Manager monitoring Android DownloadManager tasks
 * and publishing real-time % progress to the Honor Magic Capsule.
 */
object UniversalDownloadIslandManager {

    private val trackingJobs = ConcurrentHashMap<Long, Job>()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun trackDownload(context: Context, downloadId: Long, customTitle: String = "Tệp tin") {
        val prefs = AppFeaturePreferences(context)
        if (!prefs.isDownloadIslandEnabled) return

        if (trackingJobs.containsKey(downloadId)) return

        val capsuleManager = CapsuleManager(context)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return

        val job = scope.launch {
            var lastPercent = -1
            while (isActive) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                var cursor: Cursor? = null
                try {
                    cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val bytesSoFar = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val titleCol = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                        val fileName = if (titleCol != -1) cursor.getString(titleCol) ?: customTitle else customTitle

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                capsuleManager.postDownloadCapsule(100, fileName, "Đã tải xong")
                                delay(3000L)
                                capsuleManager.dismiss(CapsuleManager.ID_DOWNLOAD)
                                break
                            }
                            DownloadManager.STATUS_FAILED -> {
                                capsuleManager.postDownloadCapsule(0, fileName, "Tải xuống thất bại")
                                delay(3000L)
                                capsuleManager.dismiss(CapsuleManager.ID_DOWNLOAD)
                                break
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                val percent = if (totalBytes > 0) ((bytesSoFar * 100) / totalBytes).toInt() else 0
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    val mbSoFar = bytesSoFar / (1024 * 1024f)
                                    val mbTotal = totalBytes / (1024 * 1024f)
                                    val speedText = String.format("%.1f / %.1f MB", mbSoFar, mbTotal)
                                    capsuleManager.postDownloadCapsule(percent, fileName, speedText)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    break
                } finally {
                    cursor?.close()
                }
                delay(600L)
            }
            trackingJobs.remove(downloadId)
        }
        trackingJobs[downloadId] = job
    }

    fun onDownloadCompleted(context: Context, downloadId: Long) {
        val capsuleManager = CapsuleManager(context)
        capsuleManager.postDownloadCapsule(100, "Hoàn tất", "Đã tải xuống thành công")
        scope.launch {
            delay(3000L)
            capsuleManager.dismiss(CapsuleManager.ID_DOWNLOAD)
        }
        trackingJobs[downloadId]?.cancel()
        trackingJobs.remove(downloadId)
    }
}
