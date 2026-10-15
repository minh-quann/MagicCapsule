package com.buwin.magiccapsule.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.buwin.magiccapsule.MainActivity
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences

/**
 * Quick Settings Tile to quickly toggle Magic Capsule from the notification shade.
 */
class KeepAliveTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val prefs = AppFeaturePreferences(this)
        val newState = !prefs.isPowerIslandEnabled
        prefs.isPowerIslandEnabled = newState

        if (newState) {
            ChargingCapsuleService.start(this, forceTest = true)
        } else {
            ChargingCapsuleService.stop(this)
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = ChargingCapsuleService.isRunning
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Magic Capsule"
        tile.subtitle = if (isRunning) "Đang hoạt động" else "Đã tắt"
        tile.updateTile()
    }
}
