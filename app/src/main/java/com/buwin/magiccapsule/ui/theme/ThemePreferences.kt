package com.buwin.magiccapsule.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    var currentThemeMode by mutableStateOf(loadThemeMode())
        private set

    var isAutoPasteEnabled by mutableStateOf(loadAutoPaste())
        private set

    private fun loadThemeMode(): AppThemeMode {
        val saved = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(saved ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    private fun loadAutoPaste(): Boolean {
        return prefs.getBoolean("auto_detect_clipboard_link", false)
    }

    fun setThemeMode(mode: AppThemeMode) {
        currentThemeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun updateAutoPasteEnabled(enabled: Boolean) {
        isAutoPasteEnabled = enabled
        prefs.edit().putBoolean("auto_detect_clipboard_link", enabled).apply()
    }
}
