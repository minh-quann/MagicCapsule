package com.buwin.magiccapsule.service

import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences

/**
 * Quick Settings Tile to pin the currently focused app to the Magic Capsule.
 */
class PinAppTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val context = applicationContext
        val prefs = AppFeaturePreferences(context)
        val capsuleManager = CapsuleManager(context)

        val foregroundPackage = getForegroundPackageName()
        if (foregroundPackage != null && foregroundPackage != packageName) {
            val pm = packageManager
            val appLabel = try {
                val appInfo = pm.getApplicationInfo(foregroundPackage, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                foregroundPackage
            }

            prefs.pinnedPackage = foregroundPackage
            prefs.pinnedAppName = appLabel

            val launchIntent = pm.getLaunchIntentForPackage(foregroundPackage)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            val pendingIntent = launchIntent?.let {
                PendingIntent.getActivity(context, foregroundPackage.hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

            capsuleManager.postPinAppCapsule(appLabel, pendingIntent)
        } else {
            // Unpin if tapped again
            capsuleManager.dismiss(CapsuleManager.ID_PIN_APP)
            prefs.pinnedPackage = null
            prefs.pinnedAppName = null
        }
        updateTileState()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = AppFeaturePreferences(this)
        val hasPinned = prefs.pinnedPackage != null
        tile.state = if (hasPinned) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Ghim ứng dụng"
        tile.subtitle = prefs.pinnedAppName ?: "Chạm để ghim"
        tile.updateTile()
    }

    private fun getForegroundPackageName(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val time = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
