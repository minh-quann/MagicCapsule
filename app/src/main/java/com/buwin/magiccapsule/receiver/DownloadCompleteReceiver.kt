package com.buwin.magiccapsule.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.buwin.magiccapsule.service.UniversalDownloadIslandManager

/**
 * Catches system download complete notifications and updates the Capsule.
 */
class DownloadCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId != -1L) {
            UniversalDownloadIslandManager.onDownloadCompleted(context, downloadId)
        }
    }
}
