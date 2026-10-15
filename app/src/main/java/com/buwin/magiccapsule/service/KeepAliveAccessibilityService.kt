package com.buwin.magiccapsule.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.service.notification.NotificationListenerService
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

/**
 * Immortal Keep-Alive Accessibility Service designed specifically for Honor MagicOS.
 * MagicOS grants high OS process priority to active Accessibility Services,
 * preventing aggressive background memory eviction.
 */
class KeepAliveAccessibilityService : AccessibilityService() {

    private var aliveOverlayView: View? = null
    private val windowManager: WindowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        addAliveOverlay()
        rebindNotificationListener()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive listener to keep service resident with minimal CPU consumption
    }

    override fun onInterrupt() {
        // No-op
    }

    private fun addAliveOverlay() {
        try {
            removeAliveOverlay()
            val view = View(this)
            val params = WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                gravity = Gravity.TOP or Gravity.START
                width = 1
                height = 1
                packageName = this@KeepAliveAccessibilityService.packageName
            }
            windowManager.addView(view, params)
            aliveOverlayView = view
        } catch (_: Exception) {}
    }

    private fun removeAliveOverlay() {
        aliveOverlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            aliveOverlayView = null
        }
    }

    private fun rebindNotificationListener() {
        try {
            val componentName = ComponentName(this, MagicNotificationListenerService::class.java)
            NotificationListenerService.requestRebind(componentName)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        removeAliveOverlay()
        isRunning = false
        super.onDestroy()
    }

    companion object {
        var isRunning: Boolean = false
            private set
    }
}
