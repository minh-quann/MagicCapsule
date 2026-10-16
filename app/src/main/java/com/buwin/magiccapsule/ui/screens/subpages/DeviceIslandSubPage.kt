package com.buwin.magiccapsule.ui.screens.subpages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronBackward
import io.github.alexzhirkevich.cupertino.icons.outlined.Speedometer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.device.DeviceStatsReader
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.service.DeviceMonitorService
import com.buwin.magiccapsule.ui.components.liquid.LiquidButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidButtonVariant
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidSlider
import com.buwin.magiccapsule.ui.components.liquid.LiquidToggle
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@Composable
fun DeviceIslandSubPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }
    val statsReader = remember { DeviceStatsReader(context) }
    val capsuleManager = remember { CapsuleManager(context) }

    var isEnabled by remember { mutableStateOf(prefs.isDeviceIslandEnabled) }
    var showRam by remember { mutableStateOf(prefs.showRamMetric) }
    var showSpeed by remember { mutableStateOf(prefs.showNetworkSpeedMetric) }
    var showTemp by remember { mutableStateOf(prefs.showTemperatureMetric) }
    var intervalMs by remember { mutableFloatStateOf(prefs.deviceIslandIntervalMs.toFloat()) }

    var liveStats by remember { mutableStateOf(statsReader.readStats()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            liveStats = statsReader.readStats()
            delay(1000L)
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
            // Live Preview Card
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
                                imageVector = CupertinoIcons.Default.Speedometer,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Xem trước thông số trực tiếp",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1C1917)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("RAM đang dùng", fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
                                Text(
                                    "${liveStats.ramUsedPercent}% (${String.format(Locale.US, "%.1f", liveStats.ramUsedGb)}GB)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF1C1917)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tốc độ mạng", fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
                                Text(
                                    liveStats.combinedSpeedText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34C759)
                                )
                            }
                        }
                    }
                }
            }

            // Toggles Card
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
                            title = "Bật Đảo thông số thiết bị",
                            subtitle = "Tự động ghim RAM và tốc độ mạng lên Magic Capsule",
                            selected = isEnabled,
                            onSelect = {
                                isEnabled = it
                                prefs.isDeviceIslandEnabled = it
                                if (it) DeviceMonitorService.start(context) else DeviceMonitorService.stop(context)
                            },
                            isDark = isDark
                        )

                        SubpageToggleRow(
                            title = "Hiển thị phần trăm RAM",
                            subtitle = "Ví dụ: RAM 62%",
                            selected = showRam,
                            onSelect = {
                                showRam = it
                                prefs.showRamMetric = it
                            },
                            isDark = isDark
                        )

                        SubpageToggleRow(
                            title = "Hiển thị Tốc độ mạng",
                            subtitle = "Ví dụ: ↓ 2.4 MB/s hoặc ↑ 120 KB/s",
                            selected = showSpeed,
                            onSelect = {
                                showSpeed = it
                                prefs.showNetworkSpeedMetric = it
                            },
                            isDark = isDark
                        )

                        SubpageToggleRow(
                            title = "Hiển thị Nhiệt độ máy",
                            subtitle = "Theo dõi độ nóng CPU và linh kiện",
                            selected = showTemp,
                            onSelect = {
                                showTemp = it
                                prefs.showTemperatureMetric = it
                            },
                            isDark = isDark
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tần số cập nhật", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF1C1917))
                                Text("${intervalMs.toInt()} ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LiquidSlider(
                                value = { intervalMs },
                                onValueChange = {
                                    intervalMs = it
                                    prefs.deviceIslandIntervalMs = it.toLong()
                                },
                                valueRange = 1000f..5000f,
                                visibilityThreshold = 200f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Test Button
            item {
                LiquidButton(
                    onClick = {
                        val stats = statsReader.readStats()
                        capsuleManager.postDeviceStatsCapsule(
                            capsuleText = "RAM ${stats.ramUsedPercent}% · ${stats.combinedSpeedText}",
                            title = "Thông số thiết bị Honor",
                            details = "RAM: ${stats.ramUsedPercent}%  •  Mạng: ${stats.combinedSpeedText}  •  ${stats.deviceTempC}°C"
                        )
                    },
                    variant = LiquidButtonVariant.Surface,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    isDark = isDark
                ) {
                    Text("Gửi thử nghiệm Đảo thông số lên Honor Capsule", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    text = "Thông số thiết bị",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            }
        )
    }
}

@Composable
fun SubpageToggleRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: (Boolean) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF1C1917))
            Text(subtitle, fontSize = 12.sp, color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C))
        }
        LiquidToggle(selected = { selected }, onSelect = onSelect)
    }
}
