package com.buwin.magiccapsule.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.Bell
import io.github.alexzhirkevich.cupertino.icons.filled.Bolt
import io.github.alexzhirkevich.cupertino.icons.filled.Envelope
import io.github.alexzhirkevich.cupertino.icons.filled.ExclamationmarkTriangle
import io.github.alexzhirkevich.cupertino.icons.filled.Heart
import io.github.alexzhirkevich.cupertino.icons.filled.Paintpalette
import io.github.alexzhirkevich.cupertino.icons.filled.Pin
import io.github.alexzhirkevich.cupertino.icons.outlined.ArrowClockwise
import io.github.alexzhirkevich.cupertino.icons.outlined.Calendar
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronForward
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronLeftForwardslashChevronRight
import io.github.alexzhirkevich.cupertino.icons.outlined.EyeSlash
import io.github.alexzhirkevich.cupertino.icons.outlined.Faceid
import io.github.alexzhirkevich.cupertino.icons.outlined.Gearshape
import io.github.alexzhirkevich.cupertino.icons.outlined.Headphones
import io.github.alexzhirkevich.cupertino.icons.outlined.Moon
import io.github.alexzhirkevich.cupertino.icons.outlined.MusicNote
import io.github.alexzhirkevich.cupertino.icons.outlined.Shield
import io.github.alexzhirkevich.cupertino.icons.outlined.SliderHorizontal3
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
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidToggle
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.AppThemeMode
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.ThemePreferences
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Configuration & Settings screen matching NoticeFlow's Tier-1 architecture.
 * Replaces the old charging "Công suất" tab to provide full per-feature configurations.
 */
@Composable
fun SettingsConfigScreen(
    themePreferences: ThemePreferences,
    onNavigateToSubpage: (SubpageType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }

    var autoStart by remember { mutableStateOf(prefs.isAutoStartOnBoot) }
    var hideRecents by remember { mutableStateOf(prefs.isHideFromRecents) }
    var keepAliveA11y by remember { mutableStateOf(prefs.isKeepAliveA11yEnabled) }

    var powerEnabled by remember { mutableStateOf(prefs.isPowerIslandEnabled) }
    var deviceEnabled by remember { mutableStateOf(prefs.isDeviceIslandEnabled) }
    var alertEnabled by remember { mutableStateOf(prefs.isDeviceAlertEnabled) }
    var notifyEnabled by remember { mutableStateOf(prefs.isNotificationIslandEnabled) }
    var smsEnabled by remember { mutableStateOf(prefs.isSmsIslandEnabled) }
    var lyricsEnabled by remember { mutableStateOf(prefs.isLyricsIslandEnabled) }
    var headsetEnabled by remember { mutableStateOf(prefs.isHeadsetIslandEnabled) }
    var faceUnlockEnabled by remember { mutableStateOf(prefs.isFaceUnlockIslandEnabled) }
    var customCardEnabled by remember { mutableStateOf(prefs.isCustomCardPermanent) }
    var calendarEnabled by remember { mutableStateOf(prefs.isCalendarIslandEnabled) }
    var apiEnabled by remember { mutableStateOf(prefs.isApiIslandEnabled) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(top = 104.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. LÀM MỚI CÀI ĐẶT
            // ==========================================
            item {
                SectionHeader("LÀM MỚI CÀI ĐẶT", isDark)
            }
            item {
                SettingsCardContainer(isDark) {
                    ConfigNavigationRow(
                        icon = CupertinoIcons.Default.ArrowClockwise,
                        iconTint = Color(0xFF007AFF),
                        title = "Tốc độ làm mới",
                        subtitle = "Điều chỉnh tần suất cập nhật dữ liệu cho từng đảo (${prefs.refreshRateSec}s)",
                        onClick = { onNavigateToSubpage(SubpageType.REFRESH_RATE) },
                        isDark = isDark
                    )
                }
            }

            // ==========================================
            // 2. CÀI ĐẶT CHẠY NGẦM
            // ==========================================
            item {
                SectionHeader("CÀI ĐẶT CHẠY NGẦM", isDark)
            }
            item {
                SettingsCardContainer(isDark) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfigToggleRow(
                            icon = CupertinoIcons.Default.Gearshape,
                            iconTint = Color(0xFF5856D6),
                            title = "Tự khởi động",
                            subtitle = "Khởi chạy dịch vụ viên thuốc cùng hệ thống MagicOS khi bật nguồn",
                            selected = autoStart,
                            onSelect = {
                                autoStart = it
                                prefs.isAutoStartOnBoot = it
                            },
                            isDark = isDark
                        )

                        ConfigActionRow(
                            icon = CupertinoIcons.Default.Shield,
                            iconTint = Color(0xFF34C759),
                            title = "Danh sách trắng pin",
                            subtitle = "Tắt tối ưu hóa pin để tránh bị MagicOS đóng băng tiến trình ngầm",
                            actionText = "Cài đặt",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                    }
                                }
                            },
                            isDark = isDark
                        )

                        ConfigToggleRow(
                            icon = CupertinoIcons.Default.EyeSlash,
                            iconTint = Color(0xFFFF9500),
                            title = "Ẩn khỏi danh sách ứng dụng gần đây",
                            subtitle = "Giữ app hoàn toàn vô hình trong trình đa nhiệm khi thoát",
                            selected = hideRecents,
                            onSelect = {
                                hideRecents = it
                                prefs.isHideFromRecents = it
                            },
                            isDark = isDark
                        )

                        ConfigToggleRow(
                            icon = CupertinoIcons.Default.SliderHorizontal3,
                            iconTint = Color(0xFFAF52DE),
                            title = "Dịch vụ trợ năng giữ ngầm",
                            subtitle = "Duy trì kết nối Accessibility để tự động phục hồi nếu dịch vụ bị ngắt",
                            selected = keepAliveA11y,
                            onSelect = {
                                keepAliveA11y = it
                                prefs.isKeepAliveA11yEnabled = it
                            },
                            isDark = isDark
                        )
                    }
                }
            }

            // ==========================================
            // 3. THÔNG BÁO VÀ TIN NHẮN
            // ==========================================
            item {
                SectionHeader("THÔNG BÁO VÀ TIN NHẮN", isDark)
            }
            item {
                SettingsCardContainer(isDark) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Filled.Bell,
                            iconTint = Color(0xFF007AFF),
                            title = "Thông báo ra đảo",
                            subtitle = "Chọn ứng dụng, tự động ẩn biểu ngữ gốc, chỉ hiện Magic Capsule",
                            selected = notifyEnabled,
                            onSelect = {
                                notifyEnabled = it
                                prefs.isNotificationIslandEnabled = it
                            },
                            onClick = { onNavigateToSubpage(SubpageType.NOTIFY) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Filled.Envelope,
                            iconTint = Color(0xFF34C759),
                            title = "Nhận dạng SMS",
                            subtitle = "Tự động trích xuất mã OTP, mã vận đơn bưu kiện với nút sao chép 1 chạm",
                            selected = smsEnabled,
                            onSelect = {
                                smsEnabled = it
                                prefs.isSmsIslandEnabled = it
                            },
                            onClick = { onNavigateToSubpage(SubpageType.SMS) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Default.MusicNote,
                            iconTint = Color(0xFFFF2D55),
                            title = "Lời bài hát",
                            subtitle = "Đồng bộ lời bài hát thời gian thực và điều khiển nhạc từ Spotify, Zing MP3",
                            selected = lyricsEnabled,
                            onSelect = {
                                lyricsEnabled = it
                                prefs.isLyricsIslandEnabled = it
                            },
                            onClick = { onNavigateToSubpage(SubpageType.LYRICS) },
                            isDark = isDark
                        )
                    }
                }
            }

            // ==========================================
            // 4. ĐẶC ĐIỂM ĐẢO
            // ==========================================
            item {
                SectionHeader("ĐẶC ĐIỂM ĐẢO", isDark)
            }
            item {
                SettingsCardContainer(isDark) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Filled.Bolt,
                            iconTint = Color(0xFF34C759),
                            title = "Đảo công suất",
                            subtitle = "Đo công suất sạc W thực tế, dòng sạc mA và nhiệt độ pin khi cắm sạc",
                            selected = powerEnabled,
                            onSelect = {
                                powerEnabled = it
                                prefs.isPowerIslandEnabled = it
                                if (it) ChargingCapsuleService.start(context) else ChargingCapsuleService.stop(context)
                            },
                            onClick = { onNavigateToSubpage(SubpageType.POWER) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Default.Speedometer,
                            iconTint = Color(0xFF007AFF),
                            title = "Đảo thông tin thiết bị",
                            subtitle = "Giám sát RAM sử dụng, CPU và tốc độ mạng thời gian thực",
                            selected = deviceEnabled,
                            onSelect = {
                                deviceEnabled = it
                                prefs.isDeviceIslandEnabled = it
                                if (it) DeviceMonitorService.start(context) else DeviceMonitorService.stop(context)
                            },
                            onClick = { onNavigateToSubpage(SubpageType.DEVICE) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Filled.ExclamationmarkTriangle,
                            iconTint = Color(0xFFFF9500),
                            title = "Đảo nhắc nhở thiết bị",
                            subtitle = "Cảnh báo khi pin quá nóng (> 42°C), pin yếu hoặc RAM quá tải",
                            selected = alertEnabled,
                            onSelect = {
                                alertEnabled = it
                                prefs.isDeviceAlertEnabled = it
                            },
                            onClick = { onNavigateToSubpage(SubpageType.ALERT) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Filled.Heart,
                            iconTint = Color(0xFFFF2D55),
                            title = "Đảo nhịp tim",
                            subtitle = "Kết nối thiết bị đo nhịp tim Bluetooth BLE hiển thị trực tiếp lên đảo",
                            selected = false,
                            onSelect = {},
                            onClick = { onNavigateToSubpage(SubpageType.HEART_RATE) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Default.Headphones,
                            iconTint = Color(0xFFAF52DE),
                            title = "Đảo Bluetooth",
                            subtitle = "Tự động hiện tên tai nghe và phần trăm pin khi kết nối",
                            selected = headsetEnabled,
                            onSelect = {
                                headsetEnabled = it
                                prefs.isHeadsetIslandEnabled = it
                            },
                            onClick = { onNavigateToSubpage(SubpageType.HEADSET) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Default.Faceid,
                            iconTint = Color(0xFF007AFF),
                            title = "Đảo mở khóa khuôn mặt",
                            subtitle = "Chớp biểu tượng xác thực khuôn mặt thành công trong 2-3 giây",
                            selected = faceUnlockEnabled,
                            onSelect = {
                                faceUnlockEnabled = it
                                prefs.isFaceUnlockIslandEnabled = it
                                if (it) FaceUnlockMonitorService.start(context) else FaceUnlockMonitorService.stop(context)
                            },
                            onClick = { onNavigateToSubpage(SubpageType.FACE_UNLOCK) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Filled.Pin,
                            iconTint = Color(0xFFFF9500),
                            title = "Đảo thường trực",
                            subtitle = "Ghim một thẻ ghi chú tùy chỉnh hiển thị cố định 24/7 trên đảo",
                            selected = customCardEnabled,
                            onSelect = {
                                customCardEnabled = it
                                prefs.isCustomCardPermanent = it
                            },
                            onClick = { onNavigateToSubpage(SubpageType.CUSTOM_CARD) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Default.Calendar,
                            iconTint = Color(0xFF5856D6),
                            title = "Lịch trình đếm ngược",
                            subtitle = "Đếm ngược các sự kiện sắp diễn ra trong ngày từ lịch Google / Honor",
                            selected = calendarEnabled,
                            onSelect = {
                                calendarEnabled = it
                                prefs.isCalendarIslandEnabled = it
                                if (it) CalendarLiveUpdateService.start(context) else CalendarLiveUpdateService.stop(context)
                            },
                            onClick = { onNavigateToSubpage(SubpageType.CALENDAR) },
                            isDark = isDark
                        )

                        ConfigItemWithToggle(
                            icon = CupertinoIcons.Default.ChevronLeftForwardslashChevronRight,
                            iconTint = Color(0xFF34C759),
                            title = "Đảo API Webhook",
                            subtitle = "Gửi dữ liệu tùy chỉnh từ bên thứ ba hoặc máy tính lên viên thuốc qua HTTP",
                            selected = apiEnabled,
                            onSelect = {
                                apiEnabled = it
                                prefs.isApiIslandEnabled = it
                                if (it) ApiLiveUpdateService.start(context) else ApiLiveUpdateService.stop(context)
                            },
                            onClick = { onNavigateToSubpage(SubpageType.API) },
                            isDark = isDark
                        )
                    }
                }
            }

            // ==========================================
            // 5. ĐẢO VÀ SỰ XUẤT HIỆN
            // ==========================================
            item {
                SectionHeader("ĐẢO VÀ SỰ XUẤT HIỆN", isDark)
            }
            item {
                SettingsCardContainer(isDark) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfigNavigationRow(
                            icon = CupertinoIcons.Default.SliderHorizontal3,
                            iconTint = Color(0xFF007AFF),
                            title = "Hành vi đảo",
                            subtitle = "Tự động tắt khi khóa màn hình, thời gian chờ và chế độ hiển thị",
                            onClick = { onNavigateToSubpage(SubpageType.ISLAND_BEHAVIOR) },
                            isDark = isDark
                        )

                        ConfigNavigationRow(
                            icon = CupertinoIcons.Filled.Paintpalette,
                            iconTint = Color(0xFFFF2D55),
                            title = "Cài đặt chủ đề",
                            subtitle = "Tùy biến hiệu ứng Liquid Glass quang học, độ mờ đục và bán kính bo góc",
                            onClick = { onNavigateToSubpage(SubpageType.THEME) },
                            isDark = isDark
                        )

                        ConfigNavigationRow(
                            icon = CupertinoIcons.Default.Speedometer,
                            iconTint = Color(0xFFFF9500),
                            title = "Cài đặt cuộn chữ",
                            subtitle = "Tốc độ cuộn chữ khi tiêu đề thông báo hoặc lời bài hát quá dài",
                            onClick = { onNavigateToSubpage(SubpageType.SCROLL) },
                            isDark = isDark
                        )
                    }
                }
            }

            // ==========================================
            // 6. THÔNG TIN ỨNG DỤNG
            // ==========================================
            item {
                SectionHeader("THÔNG TIN ỨNG DỤNG", isDark)
            }
            item {
                SettingsCardContainer(isDark) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Phiên bản", fontSize = 14.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
                            Text("3.5.2 (Tương thích Honor MagicOS 11 / API 37)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1C1917))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Thiết bị", fontSize = 14.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
                            Text("${Build.MANUFACTURER.uppercase()} ${Build.MODEL}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                        }
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
            title = {
                Text(
                    text = "Cài đặt cấu hình",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            },
            actions = {
                LiquidRoundButton(
                    onClick = {
                        val currentMode = themePreferences.currentThemeMode
                        val newMode = when (currentMode) {
                            AppThemeMode.LIGHT -> AppThemeMode.DARK
                            AppThemeMode.DARK -> AppThemeMode.SYSTEM
                            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                        }
                        themePreferences.setThemeMode(newMode)
                    },
                    size = 38.dp,
                    isDark = isDark
                ) {
                    Icon(
                        imageVector = if (isDark) CupertinoIcons.Default.SunMax else CupertinoIcons.Default.Moon,
                        contentDescription = "Chuyển giao diện",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, isDark: Boolean) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsCardContainer(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
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
        content()
    }
}

@Composable
private fun ConfigNavigationRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1C1917))
                Text(text = subtitle, fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
            }
        }
        Icon(
            imageVector = CupertinoIcons.Default.ChevronForward,
            contentDescription = null,
            tint = if (isDark) Color(0xFF57534E) else Color(0xFFD6D3D1),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ConfigToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: (Boolean) -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1C1917))
                Text(text = subtitle, fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
            }
        }
        LiquidToggle(checked = selected, onCheckedChange = onSelect)
    }
}

@Composable
private fun ConfigActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    actionText: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1C1917))
                Text(text = subtitle, fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF007AFF).copy(alpha = 0.15f))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
        }
    }
}

@Composable
private fun ConfigItemWithToggle(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: (Boolean) -> Unit,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1C1917))
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        imageVector = CupertinoIcons.Default.ChevronForward,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF78716C) else Color(0xFFA8A29E),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(text = subtitle, fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
            }
        }
        LiquidToggle(checked = selected, onCheckedChange = onSelect)
    }
}
