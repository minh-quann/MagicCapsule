package com.buwin.magiccapsule.ui.screens.subpages

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.Bell
import io.github.alexzhirkevich.cupertino.icons.filled.Bolt
import io.github.alexzhirkevich.cupertino.icons.filled.BubbleLeft
import io.github.alexzhirkevich.cupertino.icons.filled.CheckmarkCircle
import io.github.alexzhirkevich.cupertino.icons.filled.ExclamationmarkTriangle
import io.github.alexzhirkevich.cupertino.icons.filled.Heart
import io.github.alexzhirkevich.cupertino.icons.filled.Phone
import io.github.alexzhirkevich.cupertino.icons.outlined.ArrowClockwise
import io.github.alexzhirkevich.cupertino.icons.outlined.Bolt
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronBackward
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronDown
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronUp
import io.github.alexzhirkevich.cupertino.icons.outlined.Grid
import io.github.alexzhirkevich.cupertino.icons.outlined.Shield
import io.github.alexzhirkevich.cupertino.icons.outlined.SliderHorizontal3
import com.buwin.magiccapsule.data.notification.ShizukuChannelScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.notification.DynamicAppChannel
import com.buwin.magiccapsule.data.notification.NotificationChannelRepository
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.service.MagicNotificationListenerService
import com.buwin.magiccapsule.ui.components.liquid.LiquidButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidButtonVariant
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidSlider
import com.buwin.magiccapsule.ui.components.liquid.LiquidToggle
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val iconBitmap: Bitmap?,
    val isPriorityChat: Boolean
)

@Composable
fun NotificationIslandSubPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }
    val capsuleManager = remember { CapsuleManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var isEnabled by remember { mutableStateOf(prefs.isNotificationIslandEnabled) }
    var filterImportant by remember { mutableStateOf(prefs.filterImportantOnly) }
    var durationSec by remember { mutableFloatStateOf(prefs.notificationDurationSec.toFloat()) }

    var allowedAppsSet by remember { mutableStateOf(prefs.notificationAllowedApps) }

    var isServiceConnected by remember { mutableStateOf(MagicNotificationListenerService.isListenerConnected) }
    var isShizukuRunning by remember { mutableStateOf(ShizukuChannelScanner.isShizukuInstalledAndRunning()) }
    var hasShizukuPermission by remember { mutableStateOf(ShizukuChannelScanner.hasShizukuPermission()) }
    var expandedAppPackage by remember { mutableStateOf<String?>(null) }
    var channelConfigVersion by remember { mutableIntStateOf(0) }

    val installedAppsList = remember { mutableStateListOf<InstalledAppItem>() }

    // Periodically verify listener service connection & auto-rebind & Shizuku state
    LaunchedEffect(Unit) {
        MagicNotificationListenerService.ensureServiceBound(context)
        while (isActive) {
            isServiceConnected = MagicNotificationListenerService.isListenerConnected
            isShizukuRunning = ShizukuChannelScanner.isShizukuInstalledAndRunning()
            hasShizukuPermission = ShizukuChannelScanner.hasShizukuPermission()
            delay(1500)
        }
    }

    // Load installed messaging & popular apps in background
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val items = ArrayList<InstalledAppItem>()

            val commonChatPkgs = setOf(
                "com.grabtaxi.passenger",
                "com.shopee.vn",
                "com.zing.zalo",
                "com.facebook.orca",
                "org.telegram.messenger",
                "com.whatsapp",
                "com.google.android.apps.messaging",
                "com.viber.voip",
                "com.google.android.gm",
                "com.instagram.android",
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
                "com.lazada.android",
                "com.mservice.momotransfer",
                "com.VCB",
                "vn.com.techcombank.bb.app"
            )

            val seenPkgs = HashSet<String>()
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName || seenPkgs.contains(pkg)) continue
                seenPkgs.add(pkg)

                val label = try {
                    info.loadLabel(pm).toString()
                } catch (_: Exception) {
                    pkg
                }

                val iconBitmap = try {
                    drawableToBitmap(info.loadIcon(pm))
                } catch (_: Exception) {
                    null
                }

                val isCommon = commonChatPkgs.contains(pkg) ||
                        pkg.contains("zalo", ignoreCase = true) ||
                        pkg.contains("messenger", ignoreCase = true) ||
                        pkg.contains("telegram", ignoreCase = true) ||
                        pkg.contains("grab", ignoreCase = true) ||
                        pkg.contains("shopee", ignoreCase = true)

                items.add(
                    InstalledAppItem(
                        packageName = pkg,
                        appName = label,
                        iconBitmap = iconBitmap,
                        isPriorityChat = isCommon
                    )
                )
            }

            // Sort chat apps first, then alphabetically
            items.sortWith(compareByDescending<InstalledAppItem> { it.isPriorityChat }.thenBy { it.appName.lowercase() })

            withContext(Dispatchers.Main) {
                installedAppsList.clear()
                installedAppsList.addAll(items)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(top = 104.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview card
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
                            Icon(
                                imageVector = CupertinoIcons.Filled.Bell,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Đảo thông báo ứng dụng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1C1917)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Đưa thông báo tin nhắn & cuộc gọi lên Magic Capsule trên Honor MagicOS. Cho phép đọc trực tiếp toàn bộ các kênh/thể loại thông báo thực tế của từng ứng dụng từ hệ điều hành, tùy chọn bật/tắt từng kênh và ẩn hoàn toàn biểu ngữ popup che màn hình.",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                        )
                    }
                }
            }

            // Service Connection Status & Permission Guidance
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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isServiceConnected) CupertinoIcons.Filled.CheckmarkCircle else CupertinoIcons.Filled.ExclamationmarkTriangle,
                                contentDescription = null,
                                tint = if (isServiceConnected) Color(0xFF34C759) else Color(0xFFFF9500),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isServiceConnected) "Dịch vụ thông báo: Đang hoạt động" else "Dịch vụ thông báo chưa liên kết",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF1C1917)
                                )
                                Text(
                                    text = if (isServiceConnected)
                                        "Đã kết nối với Android NotificationListener để đọc và chuyển tiếp thông báo"
                                    else
                                        "Nếu vừa cài lại app hoặc không thấy đảo nhảy, vui lòng bấm Kích hoạt lại hoặc cấp lại quyền",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LiquidButton(
                                onClick = {
                                    MagicNotificationListenerService.ensureServiceBound(context)
                                    coroutineScope.launch {
                                        delay(500)
                                        isServiceConnected = MagicNotificationListenerService.isListenerConnected
                                    }
                                },
                                variant = LiquidButtonVariant.Tinted,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.weight(1f).height(40.dp),
                                isDark = isDark
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = CupertinoIcons.Default.ArrowClockwise, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                    Text("Kích hoạt lại", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            LiquidButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                },
                                variant = LiquidButtonVariant.Surface,
                                modifier = Modifier.weight(1f).height(40.dp),
                                isDark = isDark
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = CupertinoIcons.Default.Shield, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Text("Cài đặt quyền", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Shizuku Privileged System Scanner Card
            item {
                val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
                val borderColor = cardBorderColor(isDark)
                var isShizukuScanning by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = CupertinoIcons.Default.Bolt,
                                contentDescription = null,
                                tint = if (hasShizukuPermission) Color(0xFF34C759) else Color(0xFF007AFF),
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasShizukuPermission)
                                        "⚡ Quét sâu hệ thống (Shizuku: Đã cấp quyền)"
                                    else if (isShizukuRunning)
                                        "⚡ Phát hiện Shizuku trên máy"
                                    else
                                        "⚡ Quét thể loại nâng cao (Shizuku)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF1C1917)
                                )
                                Text(
                                    text = if (hasShizukuPermission)
                                        "Đã có quyền đọc trực tiếp 100% tất cả các kênh thông báo của mọi app (Grab, Zalo, Ngân hàng...) từ hệ điều hành Android."
                                    else if (isShizukuRunning)
                                        "Cấp quyền Shizuku 1 chạm để tự động trích xuất toàn bộ kênh thông báo hệ thống của tất cả ứng dụng mà không cần root."
                                    else
                                        "Magic Capsule đã nạp sẵn danh mục kênh cho Grab, Zalo, Shopee, Telegram, Ngân hàng... Nếu cài Shizuku sẽ quét được thêm toàn bộ các app khác.",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                                )
                            }
                        }

                        if (hasShizukuPermission) {
                            LiquidButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isShizukuScanning = true
                                        val scanned = ShizukuChannelScanner.scanAllSystemChannels(context)
                                        channelConfigVersion++
                                        isShizukuScanning = false
                                    }
                                },
                                variant = LiquidButtonVariant.Tinted,
                                tint = Color(0xFF34C759),
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                isDark = isDark
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = CupertinoIcons.Default.ArrowClockwise, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                    Text(
                                        text = if (isShizukuScanning) "Đang quét toàn bộ hệ thống..." else "Quét sâu toàn bộ kênh thông báo hệ thống",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else if (isShizukuRunning) {
                            LiquidButton(
                                onClick = {
                                    ShizukuChannelScanner.requestShizukuPermission()
                                },
                                variant = LiquidButtonVariant.Tinted,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                isDark = isDark
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = CupertinoIcons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                    Text("Cấp quyền Shizuku 1 chạm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }


            // Master Toggles Card
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
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SubpageToggleRow(
                            title = "Bật chuyển tiếp thông báo lên đảo",
                            subtitle = "Kích hoạt hiệu ứng nổi viên thuốc khi có tin nhắn & cuộc gọi mới",
                            selected = isEnabled,
                            onSelect = {
                                isEnabled = it
                                prefs.isNotificationIslandEnabled = it
                            },
                            isDark = isDark
                        )


                        SubpageToggleRow(
                            title = "Chỉ lọc tin nhắn quan trọng",
                            subtitle = "Bỏ qua các thông báo quảng cáo rác không cần thiết",
                            selected = filterImportant,
                            onSelect = {
                                filterImportant = it
                                prefs.filterImportantOnly = it
                            },
                            isDark = isDark
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Thời gian hiển thị trên đảo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF1C1917))
                                Text("${durationSec.toInt()} giây", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LiquidSlider(
                                value = { durationSec },
                                onValueChange = {
                                    durationSec = it
                                    prefs.notificationDurationSec = it.toInt()
                                },
                                valueRange = 3f..15f,
                                visibilityThreshold = 1f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }



            // App Whitelist Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(imageVector = CupertinoIcons.Default.Grid, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(18.dp))
                    Text(
                        text = "CẤU HÌNH KÊNH THÔNG BÁO THỰC TẾ TỪNG ỨNG DỤNG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                    )
                }
            }

            // Installed apps list with dynamic system channel discovery
            items(installedAppsList, key = { it.packageName }) { appItem ->
                val cardBg = if (isDark) Color(0xFF141416) else Color(0xFFFFFFFF)
                val borderColor = cardBorderColor(isDark)
                val isAppAllowed = allowedAppsSet.contains(appItem.packageName)
                val isExpanded = expandedAppPackage == appItem.packageName

                @Suppress("UNUSED_VARIABLE")
                val trigger = channelConfigVersion

                var appChannels by remember(appItem.packageName) {
                    mutableStateOf<List<DynamicAppChannel>>(emptyList())
                }
                var isLoadingChannels by remember(appItem.packageName) { mutableStateOf(false) }

                LaunchedEffect(appItem.packageName, isExpanded, channelConfigVersion) {
                    if (isExpanded) {
                        isLoadingChannels = true
                        val channels = withContext(Dispatchers.IO) {
                            NotificationChannelRepository.getChannelsForPackage(context, appItem.packageName)
                        }
                        appChannels = channels
                        isLoadingChannels = false
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        // Header row: app icon, name, expand indicator, and master toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedAppPackage = if (isExpanded) null else appItem.packageName
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (appItem.iconBitmap != null) {
                                    Image(
                                        bitmap = appItem.iconBitmap.asImageBitmap(),
                                        contentDescription = appItem.appName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF007AFF).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CupertinoIcons.Filled.Bell,
                                            contentDescription = null,
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appItem.appName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color(0xFF1C1917)
                                        )
                                        if (appItem.isPriorityChat) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF34C759).copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("Tin nhắn", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34C759))
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (isExpanded) "Thu gọn thể loại" else "Chạm để xem các thể loại thông báo",
                                            fontSize = 11.sp,
                                            color = Color(0xFF007AFF)
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) CupertinoIcons.Default.ChevronUp else CupertinoIcons.Default.ChevronDown,
                                            contentDescription = null,
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Master toggle for this app
                            LiquidToggle(
                                checked = isAppAllowed,
                                onCheckedChange = { allowed ->
                                    prefs.setAppAllowedForCapsule(appItem.packageName, allowed)
                                    allowedAppsSet = prefs.notificationAllowedApps
                                }
                            )
                        }

                        // Expandable per-app configuration
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                                )



                                // Real System Channels Header with dynamic count and re-scan button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = CupertinoIcons.Default.SliderHorizontal3,
                                            contentDescription = null,
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (appChannels.isNotEmpty())
                                                "CÁC KÊNH THÔNG BÁO HỆ THỐNG (${appChannels.size} thể loại):"
                                            else
                                                "CÁC KÊNH THÔNG BÁO HỆ THỐNG:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                                        )
                                    }

                                    Text(
                                        text = "Quét lại",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF007AFF),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                coroutineScope.launch {
                                                    isLoadingChannels = true
                                                    val updated = withContext(Dispatchers.IO) {
                                                        if (ShizukuChannelScanner.hasShizukuPermission()) {
                                                            ShizukuChannelScanner.scanAllSystemChannels(context)
                                                        }
                                                        NotificationChannelRepository.getChannelsForPackage(context, appItem.packageName)
                                                    }
                                                    if (updated.isNotEmpty()) {
                                                        appChannels = updated
                                                    }
                                                    isLoadingChannels = false
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (isLoadingChannels && appChannels.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Đang đọc các thể loại thông báo từ hệ thống...",
                                            fontSize = 12.sp,
                                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                                        )
                                    }
                                } else if (appChannels.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Chưa phát hiện kênh thông báo riêng từ ứng dụng này trên Android. Kênh sẽ tự động hiển thị tại đây khi ứng dụng gửi thông báo đầu tiên hoặc bấm Quét lại.",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                                        )
                                    }
                                } else {
                                    appChannels.forEach { ch ->
                                        val isChannelAllowed = prefs.isAppChannelAllowed(appItem.packageName, ch.id)
                                        val chIcon = when {
                                            ch.id.contains("call", true) || ch.name.contains("gọi", true) || ch.name.contains("call", true) || ch.name.contains("voip", true) -> CupertinoIcons.Filled.Phone
                                            ch.id.contains("chat", true) || ch.id.contains("msg", true) || ch.name.contains("tin nhắn", true) || ch.name.contains("chat", true) || ch.name.contains("message", true) -> CupertinoIcons.Filled.BubbleLeft
                                            ch.id.contains("like", true) || ch.id.contains("comment", true) || ch.name.contains("thích", true) || ch.name.contains("bình luận", true) || ch.name.contains("tương tác", true) -> CupertinoIcons.Filled.Heart
                                            ch.id.contains("live", true) || ch.id.contains("activity", true) || ch.id.contains("trans", true) || ch.id.contains("order", true) -> CupertinoIcons.Filled.Bolt
                                            else -> CupertinoIcons.Filled.Bell
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = chIcon,
                                                    contentDescription = null,
                                                    tint = if (isChannelAllowed) Color(0xFF007AFF) else Color(0xFF78716C),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = ch.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isDark) Color.White else Color(0xFF1C1917)
                                                        )
                                                        if (ch.groupName != null) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "• ${ch.groupName}",
                                                                fontSize = 11.sp,
                                                                color = if (isDark) Color(0xFF78716C) else Color(0xFFA8A29E)
                                                             )
                                                        }
                                                    }
                                                    Text(
                                                        text = ch.description.ifBlank { "Mã kênh: ${ch.id}" },
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        color = if (isDark) Color(0xFF78716C) else Color(0xFFA8A29E)
                                                    )
                                                }
                                            }

                                            LiquidToggle(
                                                checked = isChannelAllowed,
                                                onCheckedChange = { allowed ->
                                                    prefs.setAppChannelAllowed(appItem.packageName, ch.id, allowed)
                                                    channelConfigVersion++
                                                }
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                                )

                                LiquidButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, appItem.packageName)
                                        }
                                        context.startActivity(intent)
                                    },
                                    variant = LiquidButtonVariant.Surface,
                                    modifier = Modifier.fillMaxWidth().height(38.dp),
                                    isDark = isDark
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(imageVector = CupertinoIcons.Default.SliderHorizontal3, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Text("Mở cài đặt thông báo của ${appItem.appName} trong máy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
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
            navigationIcon = {
                LiquidRoundButton(onClick = onBack, size = 38.dp, isDark = isDark) {
                    Icon(imageVector = CupertinoIcons.Default.ChevronBackward, contentDescription = "Quay lại", modifier = Modifier.size(18.dp))
                }
            },
            title = {
                Text(
                    text = "Thông báo ứng dụng",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            }
        )
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
