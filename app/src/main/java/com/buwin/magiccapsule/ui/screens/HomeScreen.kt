package com.buwin.magiccapsule.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.magiccapsule.data.battery.BatteryStatsReader
import com.buwin.magiccapsule.data.model.BatteryInfo
import com.buwin.magiccapsule.data.model.PluggedType
import com.buwin.magiccapsule.data.preferences.CapsuleFormat
import com.buwin.magiccapsule.data.preferences.CapsulePreferences
import com.buwin.magiccapsule.service.ChargingCapsuleService
import com.buwin.magiccapsule.ui.components.liquid.LiquidButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidButtonVariant
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidSlider
import com.buwin.magiccapsule.ui.components.liquid.LiquidToggle
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.AppThemeMode
import com.buwin.magiccapsule.ui.theme.BackgroundDark
import com.buwin.magiccapsule.ui.theme.BackgroundLight
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.PrimaryBlue
import com.buwin.magiccapsule.ui.theme.PrimaryBlueDark
import com.buwin.magiccapsule.ui.theme.ThemePreferences
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

/**
 * Main dashboard showcasing real-time charging telemetry and Honor Magic Capsule configuration.
 */
@Composable
fun HomeScreen(
    themePreferences: ThemePreferences,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()

    val prefs = remember { CapsulePreferences(context) }
    val batteryReader = remember { BatteryStatsReader(context) }

    var batteryInfo by remember { mutableStateOf(batteryReader.readBatteryInfo()) }
    var isCapsuleServiceRunning by remember { mutableStateOf(ChargingCapsuleService.isRunning) }

    // Settings state backed by SharedPreferences
    var autoStart by remember { mutableStateOf(prefs.autoStartOnPlug) }
    var autoDismiss by remember { mutableStateOf(prefs.autoDismissWhenFull) }
    var showPercent by remember { mutableStateOf(prefs.showBatteryPercent) }
    var showVoltage by remember { mutableStateOf(prefs.showVoltage) }
    var showCurrent by remember { mutableStateOf(prefs.showCurrent) }
    var showTemp by remember { mutableStateOf(prefs.showTemperature) }
    var capsuleFormat by remember { mutableStateOf(prefs.capsuleFormat) }
    var updateInterval by remember { mutableFloatStateOf(prefs.updateIntervalMs.toFloat()) }

    // Live telemetry polling while on-screen
    LaunchedEffect(Unit) {
        while (isActive) {
            batteryInfo = batteryReader.readBatteryInfo()
            isCapsuleServiceRunning = ChargingCapsuleService.isRunning
            delay(1000L)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Scrollable content layer
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(
                top = 114.dp,
                bottom = 48.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Banner (if not granted)
            if (!hasNotificationPermission) {
                item {
                    PermissionNoticeCard(
                        onRequestPermission = onRequestNotificationPermission,
                        backdrop = rootBackdrop,
                        isDark = isDark
                    )
                }
            }

            // Hero Card: Live Power & Telemetry
            item {
                TelemetryHeroCard(
                    info = batteryInfo,
                    isDark = isDark,
                    backdrop = rootBackdrop
                )
            }

            // Quick Action: Test Magic Capsule Now
            item {
                TestCapsuleActionCard(
                    isRunning = isCapsuleServiceRunning,
                    onToggleTest = {
                        if (isCapsuleServiceRunning) {
                            ChargingCapsuleService.stop(context)
                            isCapsuleServiceRunning = false
                        } else {
                            ChargingCapsuleService.start(context, forceTest = true)
                            isCapsuleServiceRunning = true
                        }
                    },
                    backdrop = rootBackdrop,
                    isDark = isDark
                )
            }

            // Capsule Style Selector
            item {
                CapsuleFormatSelectorCard(
                    selectedFormat = capsuleFormat,
                    onFormatSelected = { format ->
                        capsuleFormat = format
                        prefs.capsuleFormat = format
                    },
                    backdrop = rootBackdrop,
                    isDark = isDark
                )
            }

            // Settings Section
            item {
                SettingsSectionCard(
                    autoStart = autoStart,
                    onAutoStartChanged = {
                        autoStart = it
                        prefs.autoStartOnPlug = it
                    },
                    autoDismiss = autoDismiss,
                    onAutoDismissChanged = {
                        autoDismiss = it
                        prefs.autoDismissWhenFull = it
                    },
                    showPercent = showPercent,
                    onShowPercentChanged = {
                        showPercent = it
                        prefs.showBatteryPercent = it
                    },
                    showVoltage = showVoltage,
                    onShowVoltageChanged = {
                        showVoltage = it
                        prefs.showVoltage = it
                    },
                    showCurrent = showCurrent,
                    onShowCurrentChanged = {
                        showCurrent = it
                        prefs.showCurrent = it
                    },
                    showTemp = showTemp,
                    onShowTempChanged = {
                        showTemp = it
                        prefs.showTemperature = it
                    },
                    updateIntervalMs = updateInterval,
                    onUpdateIntervalChanged = {
                        updateInterval = it
                        prefs.updateIntervalMs = it.toLong()
                    },
                    backdrop = rootBackdrop,
                    isDark = isDark
                )
            }

            // Architecture & Security Card
            item {
                SecurityInfoCard(isDark = isDark)
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        // Top Bar with Liquid Glass Progressive Blur
        LiquidTopBar(
            backdrop = rootBackdrop,
            isDark = isDark,
            headerHeight = 104.dp,
            title = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Magic Capsule",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = if (isDark) Color.White else Color(0xFF1C1917)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isDark) Color(0xFF007AFF).copy(alpha = 0.25f)
                                    else Color(0xFF007AFF).copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Honor MagicOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                            )
                        }
                    }
                    Text(
                        text = "Đo công suất sạc & Dynamic Capsule",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                    )
                }
            },
            actions = {
                LiquidRoundButton(
                    onClick = {
                        val nextMode = when (themePreferences.currentThemeMode) {
                            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                            AppThemeMode.LIGHT -> AppThemeMode.DARK
                            AppThemeMode.DARK -> AppThemeMode.SYSTEM
                        }
                        themePreferences.setThemeMode(nextMode)
                    },
                    backdrop = rootBackdrop,
                    size = 40.dp,
                    isDark = isDark,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    val icon = when (themePreferences.currentThemeMode) {
                        AppThemeMode.SYSTEM -> Icons.Default.Speed
                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                        AppThemeMode.DARK -> Icons.Default.DarkMode
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Chuyển giao diện",
                        modifier = Modifier.size(18.dp),
                        tint = if (isDark) Color.White else Color(0xFF1C1917)
                    )
                }
            }
        )
    }
}

/**
 * Hero telemetry card displaying real-time charging power wattage and live specs.
 */
@Composable
private fun TelemetryHeroCard(
    info: BatteryInfo,
    isDark: Boolean,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
    val borderColor = cardBorderColor(isDark)

    val chargingColor = if (info.powerW > 40.0) {
        Color(0xFFFF9500) // SuperCharge Orange
    } else if (info.isCharging) {
        Color(0xFF34C759) // Normal fast charge Green
    } else {
        Color(0xFF8E8E93) // Idle Gray
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Charging status pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(chargingColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = chargingColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when {
                            info.isFull -> "Pin đã đầy 100%"
                            info.powerW > 40.0 -> "Honor SuperCharge · Sạc siêu nhanh"
                            info.isCharging -> "Đang sạc pin"
                            else -> "Đang dùng pin (Chưa cắm sạc)"
                        },
                        color = chargingColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Wattage Counter
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", info.powerW),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 56.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "W",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = chargingColor,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4-Telemetry Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Dòng điện",
                    value = "${info.currentMa} mA",
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Điện áp",
                    value = String.format(Locale.US, "%.2f V", info.voltageV),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Mức pin",
                    value = "${info.levelPercent}%",
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Nhiệt độ",
                    value = String.format(Locale.US, "%.1f °C", info.temperatureC),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF5F5F4)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1C1917)
            )
        }
    }
}

/**
 * Card with button to test Magic Capsule pill instantly without physical plug.
 */
@Composable
private fun TestCapsuleActionCard(
    isRunning: Boolean,
    onToggleTest: () -> Unit,
    backdrop: Backdrop,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
    val borderColor = cardBorderColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Thử nghiệm Magic Capsule",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1C1917)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isRunning) {
                    "Magic Capsule đang hoạt động trên thanh trạng thái! Bạn có thể vuốt lên màn hình chính để chiêm ngưỡng."
                } else {
                    "Kích hoạt Capsule ngay lập tức để kiểm tra hiển thị trên đảo trạng thái Honor mà không cần cắm sạc."
                },
                fontSize = 13.sp,
                color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LiquidButton(
                onClick = onToggleTest,
                backdrop = backdrop,
                variant = if (isRunning) LiquidButtonVariant.Tinted else LiquidButtonVariant.Surface,
                tint = if (isRunning) Color(0xFFFF3B30) else Color(0xFF007AFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                isDark = isDark
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Bolt else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isRunning) Color.White else (if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isRunning) "Dừng Magic Capsule" else "Bật thử Magic Capsule trên Honor",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) Color.White else (if (isDark) Color.White else Color(0xFF1C1917))
                    )
                }
            }
        }
    }
}

/**
 * Capsule Pill format selector (Wattage only vs Wattage + Current vs Wattage + Temp).
 */
@Composable
private fun CapsuleFormatSelectorCard(
    selectedFormat: CapsuleFormat,
    onFormatSelected: (CapsuleFormat) -> Unit,
    backdrop: Backdrop,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
    val borderColor = cardBorderColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Định dạng viên thuốc Capsule",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1C1917)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Chọn kiểu thông số hiển thị trực tiếp trên Dynamic Pill ở đầu màn hình:",
                fontSize = 13.sp,
                color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CapsuleFormat.entries.forEach { format ->
                val isSelected = selectedFormat == format
                val itemBg = if (isSelected) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBg)
                        .border(1.dp, border, RoundedCornerShape(12.dp))
                        .clickable { onFormatSelected(format) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = format.displayName,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                            } else {
                                if (isDark) Color.White else Color(0xFF1C1917)
                            }
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Settings Card with LiquidToggles and LiquidSlider.
 */
@Composable
private fun SettingsSectionCard(
    autoStart: Boolean,
    onAutoStartChanged: (Boolean) -> Unit,
    autoDismiss: Boolean,
    onAutoDismissChanged: (Boolean) -> Unit,
    showPercent: Boolean,
    onShowPercentChanged: (Boolean) -> Unit,
    showVoltage: Boolean,
    onShowVoltageChanged: (Boolean) -> Unit,
    showCurrent: Boolean,
    onShowCurrentChanged: (Boolean) -> Unit,
    showTemp: Boolean,
    onShowTempChanged: (Boolean) -> Unit,
    updateIntervalMs: Float,
    onUpdateIntervalChanged: (Float) -> Unit,
    backdrop: Backdrop,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
    val borderColor = cardBorderColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Tùy chỉnh & Tự động hóa",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1C1917)
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingToggleRow(
                title = "Tự động kích hoạt khi cắm sạc",
                subtitle = "Tự khởi động Magic Capsule ngay khi phát hiện cắm sạc",
                selected = autoStart,
                onSelect = onAutoStartChanged,
                backdrop = backdrop,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = "Tự tắt khi pin đầy 100%",
                subtitle = "Đóng Capsule sau khi quá trình sạc hoàn tất",
                selected = autoDismiss,
                onSelect = onAutoDismissChanged,
                backdrop = backdrop,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = "Hiển thị dòng điện (mA)",
                subtitle = "Hiển thị cường độ dòng điện trong chi tiết thông báo",
                selected = showCurrent,
                onSelect = onShowCurrentChanged,
                backdrop = backdrop,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = "Hiển thị điện áp (V)",
                subtitle = "Hiển thị điện áp pin thực tế",
                selected = showVoltage,
                onSelect = onShowVoltageChanged,
                backdrop = backdrop,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = "Hiển thị nhiệt độ pin (°C)",
                subtitle = "Theo dõi độ nóng máy để bảo vệ pin",
                selected = showTemp,
                onSelect = onShowTempChanged,
                backdrop = backdrop,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Update Frequency Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tần số làm mới thông số",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White else Color(0xFF1C1917)
                    )
                    Text(
                        text = "${updateIntervalMs.toInt()} ms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LiquidSlider(
                    value = { updateIntervalMs },
                    onValueChange = onUpdateIntervalChanged,
                    valueRange = 500f..3000f,
                    visibilityThreshold = 100f,
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: (Boolean) -> Unit,
    backdrop: Backdrop,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White else Color(0xFF1C1917)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
            )
        }
        LiquidToggle(
            selected = { selected },
            onSelect = onSelect,
            backdrop = backdrop
        )
    }
}

/**
 * Card explaining privacy & architecture benefits (no root, no accessibility).
 */
@Composable
private fun SecurityInfoCard(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
    val borderColor = cardBorderColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF34C759),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Bảo mật & Tối ưu cho Honor",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• 100% Native Honor Magic Capsule: Tận dụng trực tiếp giao thức Capsule của MagicOS.\n• Tuyệt đối KHÔNG dùng Trợ năng (Accessibility): An toàn 100% với ứng dụng Ngân hàng.\n• Không cần quyền Root hoặc Shizuku.\n• 0% tiêu hao pin khi không cắm sạc.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                )
            }
        }
    }
}

/**
 * Permission request banner.
 */
@Composable
private fun PermissionNoticeCard(
    onRequestPermission: () -> Unit,
    backdrop: Backdrop,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0xFF2C1600) else Color(0xFFFFF3E0))
            .border(1.dp, Color(0xFFFF9500).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Cần quyền thông báo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
                )
                Text(
                    text = "Cần cấp quyền thông báo để hiển thị công suất lên Magic Capsule",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFFFE0B2) else Color(0xFFBF360C)
                )
            }
            LiquidButton(
                onClick = onRequestPermission,
                backdrop = backdrop,
                variant = LiquidButtonVariant.Tinted,
                tint = Color(0xFFFF9500),
                isDark = isDark,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Cấp quyền",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
