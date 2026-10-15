package com.buwin.magiccapsule.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.ArrowDownCircle
import io.github.alexzhirkevich.cupertino.icons.filled.Bell
import io.github.alexzhirkevich.cupertino.icons.filled.Bolt
import io.github.alexzhirkevich.cupertino.icons.filled.Envelope
import io.github.alexzhirkevich.cupertino.icons.filled.Gearshape
import io.github.alexzhirkevich.cupertino.icons.filled.Headphones
import io.github.alexzhirkevich.cupertino.icons.filled.Mic
import io.github.alexzhirkevich.cupertino.icons.filled.MusicNote
import io.github.alexzhirkevich.cupertino.icons.filled.Paintpalette
import io.github.alexzhirkevich.cupertino.icons.filled.Pin
import io.github.alexzhirkevich.cupertino.icons.filled.Video
import io.github.alexzhirkevich.cupertino.icons.outlined.Calendar
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronForward
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronLeftForwardslashChevronRight
import io.github.alexzhirkevich.cupertino.icons.outlined.Faceid
import io.github.alexzhirkevich.cupertino.icons.outlined.Moon
import io.github.alexzhirkevich.cupertino.icons.outlined.Speedometer
import io.github.alexzhirkevich.cupertino.icons.outlined.SunMax
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.service.ApiLiveUpdateService
import com.buwin.magiccapsule.service.CalendarLiveUpdateService
import com.buwin.magiccapsule.service.ChargingCapsuleService
import com.buwin.magiccapsule.service.DeviceMonitorService
import com.buwin.magiccapsule.service.FaceUnlockMonitorService
import com.buwin.magiccapsule.service.LoudnessMonitorService
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidToggle
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.AppThemeMode
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.ThemePreferences
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

enum class SubpageType {
    POWER,
    DEVICE,
    SMS,
    LYRICS,
    NOTIFY,
    HEADSET,
    FACE_UNLOCK,
    CALENDAR,
    API,
    THEME,
    ALERT,
    HEART_RATE,
    CUSTOM_CARD,
    REFRESH_RATE,
    ISLAND_BEHAVIOR,
    SCROLL
}

/**
 * Main feature overview screen displaying all Honor Magic Capsule capabilities.
 */
@Composable
fun FeaturesOverviewScreen(
    themePreferences: ThemePreferences,
    onNavigateToSubpage: (SubpageType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }

    var powerEnabled by remember { mutableStateOf(prefs.isPowerIslandEnabled) }
    var deviceEnabled by remember { mutableStateOf(prefs.isDeviceIslandEnabled) }
    var smsEnabled by remember { mutableStateOf(prefs.isSmsIslandEnabled) }
    var lyricsEnabled by remember { mutableStateOf(prefs.isLyricsIslandEnabled) }
    var notifyEnabled by remember { mutableStateOf(prefs.isNotificationIslandEnabled) }
    var headsetEnabled by remember { mutableStateOf(prefs.isHeadsetIslandEnabled) }
    var faceEnabled by remember { mutableStateOf(prefs.isFaceUnlockIslandEnabled) }
    var calendarEnabled by remember { mutableStateOf(prefs.isCalendarIslandEnabled) }
    var apiEnabled by remember { mutableStateOf(prefs.isApiIslandEnabled) }
    var downloadEnabled by remember { mutableStateOf(prefs.isDownloadIslandEnabled) }
    var screenRecordEnabled by remember { mutableStateOf(prefs.isScreenRecordIslandEnabled) }
    var pinAppEnabled by remember { mutableStateOf(prefs.isPinAppIslandEnabled) }
    var loudnessEnabled by remember { mutableStateOf(prefs.isLoudnessIslandEnabled) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(top = 104.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Description Card
            item {
                val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
                val borderColor = cardBorderColor(isDark)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Trung tâm chức năng Magic Capsule",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1C1917)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bộ sưu tập 13 module Đảo thông minh Magic Capsule tối ưu hóa riêng cho Honor MagicOS.",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                        )
                    }
                }
            }

            // 1. Power / Charging
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Bolt,
                    iconTint = Color(0xFFFF9500),
                    title = "⚡ Đo công suất sạc & Pin",
                    subtitle = "Hiển thị công suất W thực tế, dòng điện mA và nhiệt độ pin khi cắm sạc",
                    isEnabled = powerEnabled,
                    onToggle = {
                        powerEnabled = it
                        prefs.isPowerIslandEnabled = it
                        if (it) ChargingCapsuleService.start(context) else ChargingCapsuleService.stop(context)
                    },
                    onClick = { onNavigateToSubpage(SubpageType.POWER) },
                    isDark = isDark
                )
            }

            // 2. Device Hardware
            item {
                FeatureCard(
                    icon = CupertinoIcons.Default.Speedometer,
                    iconTint = Color(0xFF007AFF),
                    title = "📊 Thông số thiết bị (RAM & Mạng)",
                    subtitle = "Theo dõi % RAM và tốc độ tải lên/tải xuống Internet theo thời gian thực",
                    isEnabled = deviceEnabled,
                    onToggle = {
                        deviceEnabled = it
                        prefs.isDeviceIslandEnabled = it
                        if (it) DeviceMonitorService.start(context) else DeviceMonitorService.stop(context)
                    },
                    onClick = { onNavigateToSubpage(SubpageType.DEVICE) },
                    isDark = isDark
                )
            }

            // 3. SMS & Delivery
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Envelope,
                    iconTint = Color(0xFF34C759),
                    title = "💬 Mã xác minh OTP & Bưu kiện",
                    subtitle = "Tự trích xuất mã OTP ngân hàng và mã lấy hàng bưu tá kèm nút copy 1 chạm",
                    isEnabled = smsEnabled,
                    onToggle = {
                        smsEnabled = it
                        prefs.isSmsIslandEnabled = it
                    },
                    onClick = { onNavigateToSubpage(SubpageType.SMS) },
                    isDark = isDark
                )
            }

            // 4. Media & Lyrics
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.MusicNote,
                    iconTint = Color(0xFFAF52DE),
                    title = "🎵 Bài hát & Trình phát nhạc",
                    subtitle = "Đồng bộ tên bài hát và nghệ sĩ từ Spotify, Apple Music, Zing MP3",
                    isEnabled = lyricsEnabled,
                    onToggle = {
                        lyricsEnabled = it
                        prefs.isLyricsIslandEnabled = it
                    },
                    onClick = { onNavigateToSubpage(SubpageType.LYRICS) },
                    isDark = isDark
                )
            }

            // 5. Notification Mirroring
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Bell,
                    iconTint = Color(0xFF5856D6),
                    title = "🔔 Đảo thông báo ứng dụng",
                    subtitle = "Chuyển tiếp tin nhắn từ Zalo, Telegram, Messenger lên đỉnh Dynamic Capsule",
                    isEnabled = notifyEnabled,
                    onToggle = {
                        notifyEnabled = it
                        prefs.isNotificationIslandEnabled = it
                    },
                    onClick = { onNavigateToSubpage(SubpageType.NOTIFY) },
                    isDark = isDark
                )
            }

            // 6. Bluetooth Headset
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Headphones,
                    iconTint = Color(0xFF30D158),
                    title = "🎧 Tai nghe Bluetooth",
                    subtitle = "Hiệu ứng Dynamic Island chào đón khi kết nối tai nghe kèm thời lượng pin",
                    isEnabled = headsetEnabled,
                    onToggle = {
                        headsetEnabled = it
                        prefs.isHeadsetIslandEnabled = it
                    },
                    onClick = { onNavigateToSubpage(SubpageType.HEADSET) },
                    isDark = isDark
                )
            }

            // 7. Screen Recording
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Video,
                    iconTint = Color(0xFFFF3B30),
                    title = "🔴 Ghi màn hình Honor",
                    subtitle = "Tự nhận diện trình ghi màn hình gốc MagicOS và hiển thị đồng hồ đếm giây",
                    isEnabled = screenRecordEnabled,
                    showToggle = true,
                    onToggle = {
                        screenRecordEnabled = it
                        prefs.isScreenRecordIslandEnabled = it
                    },
                    onClick = { /* Toggled directly */ },
                    isDark = isDark
                )
            }

            // 8. Universal Download
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.ArrowDownCircle,
                    iconTint = Color(0xFF007AFF),
                    title = "⬇️ Tiến độ tải xuống tệp tin",
                    subtitle = "Theo dõi phần trăm % và dung lượng tải xuống của DownloadManager",
                    isEnabled = downloadEnabled,
                    showToggle = true,
                    onToggle = {
                        downloadEnabled = it
                        prefs.isDownloadIslandEnabled = it
                    },
                    onClick = { /* Toggled directly */ },
                    isDark = isDark
                )
            }

            // 9. Face Unlock
            item {
                FeatureCard(
                    icon = CupertinoIcons.Default.Faceid,
                    iconTint = Color(0xFF007AFF),
                    title = "🔓 Mở khóa khuôn mặt Face ID",
                    subtitle = "Mô phỏng hiệu ứng mở khóa Face ID mượt mà trên viên thuốc Capsule",
                    isEnabled = faceEnabled,
                    onToggle = {
                        faceEnabled = it
                        prefs.isFaceUnlockIslandEnabled = it
                        if (it) FaceUnlockMonitorService.start(context) else FaceUnlockMonitorService.stop(context)
                    },
                    onClick = { onNavigateToSubpage(SubpageType.FACE_UNLOCK) },
                    isDark = isDark
                )
            }

            // 10. Calendar & Events
            item {
                FeatureCard(
                    icon = CupertinoIcons.Default.Calendar,
                    iconTint = Color(0xFFFF2D55),
                    title = "📅 Lịch & Sự kiện đếm ngược",
                    subtitle = "Đếm ngược thời gian đến cuộc họp và sự kiện quan trọng trong ngày",
                    isEnabled = calendarEnabled,
                    onToggle = {
                        calendarEnabled = it
                        prefs.isCalendarIslandEnabled = it
                        if (it) CalendarLiveUpdateService.start(context) else CalendarLiveUpdateService.stop(context)
                    },
                    onClick = { onNavigateToSubpage(SubpageType.CALENDAR) },
                    isDark = isDark
                )
            }

            // 11. Pin App & Quick Return
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Pin,
                    iconTint = Color(0xFFFF9500),
                    title = "📌 Ghim ứng dụng & Trở về nhanh",
                    subtitle = "Ghim ứng dụng bất kỳ lên Capsule thông qua Quick Settings Tile",
                    isEnabled = pinAppEnabled,
                    showToggle = true,
                    onToggle = {
                        pinAppEnabled = it
                        prefs.isPinAppIslandEnabled = it
                    },
                    onClick = { /* Toggled directly */ },
                    isDark = isDark
                )
            }

            // 12. Loudness Monitor
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Mic,
                    iconTint = Color(0xFF34C759),
                    title = "🔊 Đo độ ồn & Decibel môi trường",
                    subtitle = "Đo mức áp suất âm thanh môi trường theo thời gian thực (dB)",
                    isEnabled = loudnessEnabled,
                    showToggle = true,
                    onToggle = {
                        loudnessEnabled = it
                        prefs.isLoudnessIslandEnabled = it
                        if (it) LoudnessMonitorService.start(context) else LoudnessMonitorService.stop(context)
                    },
                    onClick = { /* Toggled directly */ },
                    isDark = isDark
                )
            }

            // 13. API & Webhook
            item {
                FeatureCard(
                    icon = CupertinoIcons.Default.ChevronLeftForwardslashChevronRight,
                    iconTint = Color(0xFF0088FF),
                    title = "🌐 Webhook & Broadcast API",
                    subtitle = "Cổng broadcast nhận push thông báo tùy biến từ Tasker, Termux, cURL",
                    isEnabled = apiEnabled,
                    onToggle = {
                        apiEnabled = it
                        prefs.isApiIslandEnabled = it
                        if (it) ApiLiveUpdateService.start(context) else ApiLiveUpdateService.stop(context)
                    },
                    onClick = { onNavigateToSubpage(SubpageType.API) },
                    isDark = isDark
                )
            }

            // 14. Theme & Permissions
            item {
                FeatureCard(
                    icon = CupertinoIcons.Filled.Paintpalette,
                    iconTint = Color(0xFF0A84FF),
                    title = "🎨 Giao diện & Quyền hạn hệ thống",
                    subtitle = "Chế độ Sáng/Tối OLED, hiệu ứng kính Liquid và kiểm tra quyền MagicOS",
                    isEnabled = true,
                    showToggle = false,
                    onClick = { onNavigateToSubpage(SubpageType.THEME) },
                    isDark = isDark
                )
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        // Top Bar
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
                                text = "Honor Suite",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                            )
                        }
                    }
                    Text(
                        text = "13 module Dynamic Capsule cho MagicOS",
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
                    size = 40.dp,
                    isDark = isDark,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    val icon = when (themePreferences.currentThemeMode) {
                        AppThemeMode.SYSTEM -> CupertinoIcons.Filled.Gearshape
                        AppThemeMode.LIGHT -> CupertinoIcons.Default.SunMax
                        AppThemeMode.DARK -> CupertinoIcons.Default.Moon
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

@Composable
private fun FeatureCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    showToggle: Boolean = true,
    onToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
    val borderColor = cardBorderColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            if (showToggle) {
                LiquidToggle(
                    selected = { isEnabled },
                    onSelect = onToggle
                )
            } else {
                Icon(
                    imageVector = CupertinoIcons.Default.ChevronForward,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
