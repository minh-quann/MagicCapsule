package com.buwin.magiccapsule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.buwin.magiccapsule.data.notification.ShizukuChannelScanner
import com.buwin.magiccapsule.service.KeepAliveScheduler
import com.buwin.magiccapsule.ui.navigation.MainTabContainer
import com.buwin.magiccapsule.ui.theme.AppThemeMode
import com.buwin.magiccapsule.ui.theme.MagicCapsuleTheme
import com.buwin.magiccapsule.ui.theme.ThemePreferences
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val themePreferences by lazy { ThemePreferences(this) }
    private var hasNotificationPermission by mutableStateOf(false)

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasNotificationPermission = results[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
    }

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuChannelScanner.SHIZUKU_REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                lifecycleScope.launch {
                    ShizukuChannelScanner.scanAllSystemChannels(this@MainActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        } catch (_: Throwable) {}

        checkNotificationPermission()
        requestCorePermissions()
        try {
            KeepAliveScheduler.recoverServices(this)
            com.buwin.magiccapsule.service.MagicNotificationListenerService.ensureServiceBound(this)
        } catch (_: Exception) {
            // Silently ignore if recover fails during cold start
        }

        setContent {
            val isDark = when (themePreferences.currentThemeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MagicCapsuleTheme(darkTheme = isDark) {
                MainTabContainer(
                    themePreferences = themePreferences,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestNotificationPermission = {
                        requestCorePermissions()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
        try {
            com.buwin.magiccapsule.service.MagicNotificationListenerService.ensureServiceBound(this)
        } catch (_: Exception) {}

        lifecycleScope.launch {
            if (ShizukuChannelScanner.hasShizukuPermission()) {
                ShizukuChannelScanner.scanAllSystemChannels(this@MainActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        } catch (_: Throwable) {}
    }

    private fun checkNotificationPermission() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestCorePermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissions.add(Manifest.permission.RECEIVE_SMS)
        permissions.add(Manifest.permission.READ_CALENDAR)

        permissionsLauncher.launch(permissions.toTypedArray())
    }
}
