package com.buwin.magiccapsule.service

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences

/**
 * Quick Settings Tile to jump back to the previously active application.
 */
class QuickReturnTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val prefs = AppFeaturePreferences(this)
        val targetPkg = prefs.pinnedPackage ?: getPreviousForegroundPackage()
        if (targetPkg != null) {
            val intent = packageManager.getLaunchIntentForPackage(targetPkg)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            if (intent != null) {
                try {
                    startActivityAndCollapse(intent)
                } catch (_: Exception) {}
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.label = "Trở về nhanh"
        tile.subtitle = "Quay lại ứng dụng vừa dùng"
        tile.updateTile()
    }

    private fun getPreviousForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val time = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)
        val sorted = stats?.filter { it.packageName != packageName }?.sortedByDescending { it.lastTimeUsed }
        return sorted?.firstOrNull()?.packageName
    }
}
