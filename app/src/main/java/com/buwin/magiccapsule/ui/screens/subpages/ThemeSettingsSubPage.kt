package com.buwin.magiccapsule.ui.screens.subpages

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.Bell
import io.github.alexzhirkevich.cupertino.icons.filled.Envelope
import io.github.alexzhirkevich.cupertino.icons.filled.Eye
import io.github.alexzhirkevich.cupertino.icons.filled.Mic
import io.github.alexzhirkevich.cupertino.icons.filled.Paintpalette
import io.github.alexzhirkevich.cupertino.icons.filled.Person
import io.github.alexzhirkevich.cupertino.icons.outlined.ArrowClockwise
import io.github.alexzhirkevich.cupertino.icons.outlined.Battery100
import io.github.alexzhirkevich.cupertino.icons.outlined.Calendar
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronBackward
import io.github.alexzhirkevich.cupertino.icons.outlined.Gearshape
import io.github.alexzhirkevich.cupertino.icons.outlined.Headphones
import io.github.alexzhirkevich.cupertino.icons.outlined.Moon
import io.github.alexzhirkevich.cupertino.icons.outlined.Shield
import io.github.alexzhirkevich.cupertino.icons.outlined.SunMax
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.buwin.magiccapsule.service.KeepAliveAccessibilityService
import com.buwin.magiccapsule.service.MagicNotificationListenerService
import com.buwin.magiccapsule.ui.components.liquid.LiquidButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidButtonVariant
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.AppThemeMode
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.ThemePreferences
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Diagnostic and permissions dashboard matching NoticeFlow for Honor MagicOS.
 * Provides live telemetry on system permissions, background running state, and theme settings.
 */
@Composable
fun ThemeSettingsSubPage(
    themePreferences: ThemePreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()

    // Permission states
    var isNotificationGranted by remember { mutableStateOf(false) }
    var isNotificationListenerGranted by remember { mutableStateOf(false) }
    var isBatteryOptimizationIgnored by remember { mutableStateOf(false) }
    var isAccessibilityGranted by remember { mutableStateOf(false) }
    var isSmsGranted by remember { mutableStateOf(false) }
    var isCalendarGranted by remember { mutableStateOf(false) }
    var isUsageStatsGranted by remember { mutableStateOf(false) }
    var isBluetoothGranted by remember { mutableStateOf(false) }
    var isMicGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withContext(Dispatchers.IO) {
                val notif = NotificationManagerCompat.from(context).areNotificationsEnabled()
                val listenerFlat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                val listener = listenerFlat?.contains(context.packageName) == true || MagicNotificationListenerService.isListenerConnected
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val batt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pm.isIgnoringBatteryOptimizations(context.packageName)
                } else true
                val a11yFlat = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                val a11y = a11yFlat?.contains(context.packageName) == true || KeepAliveAccessibilityService.isRunning
                val sms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                val cal = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
                } else {
                    appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
                }
                val usage = mode == AppOpsManager.MODE_ALLOWED
                val bt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                } else true
                val mic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

                isNotificationGranted = notif
                isNotificationListenerGranted = listener
                isBatteryOptimizationIgnored = batt
                isAccessibilityGranted = a11y
                isSmsGranted = sms
                isCalendarGranted = cal
                isUsageStatsGranted = usage
                isBluetoothGranted = bt
                isMicGranted = mic
            }
            delay(1500L)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(top = 104.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Mode Selector
            item {
                val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
                val borderColor = cardBorderColor(isDark)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = CupertinoIcons.Filled.Paintpalette, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(20.dp))
                            Text("Chế độ giao diện Liquid Glass", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1C1917))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeModePill(
                                title = "Tự động",
                                icon = CupertinoIcons.Default.Gearshape,
                                isSelected = themePreferences.currentThemeMode == AppThemeMode.SYSTEM,
                                onClick = { themePreferences.setThemeMode(AppThemeMode.SYSTEM) },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                            ThemeModePill(
                                title = "Sáng",
                                icon = CupertinoIcons.Default.SunMax,
                                isSelected = themePreferences.currentThemeMode == AppThemeMode.LIGHT,
                                onClick = { themePreferences.setThemeMode(AppThemeMode.LIGHT) },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                            ThemeModePill(
                                title = "Tối",
                                icon = CupertinoIcons.Default.Moon,
                                isSelected = themePreferences.currentThemeMode == AppThemeMode.DARK,
                                onClick = { themePreferences.setThemeMode(AppThemeMode.DARK) },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Permissions & Keep-Alive Header Card
            item {
                val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
                val borderColor = cardBorderColor(isDark)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = CupertinoIcons.Default.Shield, contentDescription = null, tint = Color(0xFF34C759), modifier = Modifier.size(22.dp))
                            Column {
                                Text("Trung tâm quyền hạn Honor MagicOS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1C1917))
                                Text("Đảm bảo các quyền cần thiết để mọi tính năng đảo hoạt động mượt mà và không bị hệ thống tắt ngầm.", fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 1. Notification Permission
                        PermissionActionRow(
                            icon = CupertinoIcons.Filled.Bell,
                            iconTint = Color(0xFF007AFF),
                            title = "Quyền gửi thông báo",
                            subtitle = "Cho phép gửi và ghim Dynamic Capsule lên thanh trạng thái",
                            isGranted = isNotificationGranted,
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            isDark = isDark
                        )

                        // 2. Notification Listener
                        PermissionActionRow(
                            icon = CupertinoIcons.Default.ArrowClockwise,
                            iconTint = Color(0xFFAF52DE),
                            title = "Truy cập thông báo (Notification Access)",
                            subtitle = "Cần thiết cho Đảo Âm nhạc, Đảo Zalo/Telegram và Bắt mã OTP",
                            isGranted = isNotificationListenerGranted,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            },
                            isDark = isDark
                        )

                        // 3. Battery Optimization
                        PermissionActionRow(
                            icon = CupertinoIcons.Default.Battery100,
                            iconTint = Color(0xFFFF9500),
                            title = "Bỏ qua tối ưu hóa pin",
                            subtitle = "Ngăn MagicOS tạm dừng Capsule khi màn hình tắt",
                            isGranted = isBatteryOptimizationIgnored,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                }
                            },
                            isDark = isDark
                        )

                        // 4. Accessibility Service Keep-Alive
                        PermissionActionRow(
                            icon = CupertinoIcons.Filled.Person,
                            iconTint = Color(0xFF34C759),
                            title = "Hỗ trợ tiếp cận (Chạy ngầm bất tử)",
                            subtitle = "Giữ tiến trình Magic Capsule có độ ưu tiên cao nhất trên MagicOS",
                            isGranted = isAccessibilityGranted,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            },
                            isDark = isDark
                        )

                        // 5. SMS Permission
                        PermissionActionRow(
                            icon = CupertinoIcons.Filled.Envelope,
                            iconTint = Color(0xFF30D158),
                            title = "Quyền nhận tin nhắn SMS (OTP)",
                            subtitle = "Bắt mã OTP ngân hàng và mã bưu kiện trực tiếp siêu tốc",
                            isGranted = isSmsGranted,
                            onClick = {
                                openAppSettings(context)
                            },
                            isDark = isDark
                        )

                        // 6. Calendar Permission
                        PermissionActionRow(
                            icon = CupertinoIcons.Default.Calendar,
                            iconTint = Color(0xFFFF2D55),
                            title = "Quyền đọc lịch sự kiện",
                            subtitle = "Hiển thị đếm ngược thời gian đến cuộc họp và sự kiện sắp tới",
                            isGranted = isCalendarGranted,
                            onClick = {
                                openAppSettings(context)
                            },
                            isDark = isDark
                        )

                        // 7. Usage Stats
                        PermissionActionRow(
                            icon = CupertinoIcons.Filled.Eye,
                            iconTint = Color(0xFF5856D6),
                            title = "Truy cập dữ liệu sử dụng ứng dụng",
                            subtitle = "Cần cho tính năng Ghim ứng dụng và Trở về nhanh từ QS Tile",
                            isGranted = isUsageStatsGranted,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            },
                            isDark = isDark
                        )

                        // 8. Bluetooth Connect
                        PermissionActionRow(
                            icon = CupertinoIcons.Default.Headphones,
                            iconTint = Color(0xFF007AFF),
                            title = "Quyền kết nối Bluetooth",
                            subtitle = "Đọc phần trăm pin tai nghe Bluetooth và hiển thị popup",
                            isGranted = isBluetoothGranted,
                            onClick = {
                                openAppSettings(context)
                            },
                            isDark = isDark
                        )

                        // 9. Microphone / Loudness
                        PermissionActionRow(
                            icon = CupertinoIcons.Filled.Mic,
                            iconTint = Color(0xFFFF9500),
                            title = "Quyền ghi âm Micro (Đo độ ồn)",
                            subtitle = "Chỉ dùng để tính toán mức decibel dB thực tế của môi trường",
                            isGranted = isMicGranted,
                            onClick = {
                                openAppSettings(context)
                            },
                            isDark = isDark
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        // Top Bar
        LiquidTopBar(
            backdrop = rootBackdrop,
            isDark = isDark,
            navigationIcon = {
                LiquidRoundButton(onClick = onBack, size = 38.dp, isDark = isDark) {
                    Icon(imageVector = CupertinoIcons.Default.ChevronBackward, contentDescription = "Quay lại", modifier = Modifier.size(18.dp))
                }
            },
            title = {
                Text(
                    text = "Giao diện & Quyền hạn",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            }
        )
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    try { context.startActivity(intent) } catch (_: Exception) {}
}

@Composable
private fun ThemeModePill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) {
        if (isDark) Color(0xFF007AFF).copy(alpha = 0.25f) else Color(0xFF007AFF).copy(alpha = 0.12f)
    } else {
        if (isDark) Color(0xFF1C1C1E) else Color(0xFFF5F5F4)
    }
    val border = if (isSelected) {
        if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                } else {
                    if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                },
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                } else {
                    if (isDark) Color.White else Color(0xFF1C1917)
                }
            )
        }
    }
}

@Composable
private fun PermissionActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
                if (isGranted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF34C759).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Đã cấp",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34C759)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF3B30).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Chưa cấp",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF3B30)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
            )
        }

        LiquidButton(
            onClick = onClick,
            variant = if (isGranted) LiquidButtonVariant.Surface else LiquidButtonVariant.Tinted,
            tint = if (isGranted) Color(0xFF8E8E93) else Color(0xFF007AFF),
            isDark = isDark,
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = if (isGranted) "Cài đặt" else "Cấp quyền",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGranted) (if (isDark) Color.White else Color(0xFF1C1917)) else Color.White
            )
        }
    }
}
