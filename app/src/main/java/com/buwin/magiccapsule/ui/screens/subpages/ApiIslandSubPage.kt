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
import io.github.alexzhirkevich.cupertino.icons.filled.Paperplane
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronBackward
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronLeftForwardslashChevronRight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.magiccapsule.capsule.CapsuleManager
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.ui.components.liquid.LiquidButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidButtonVariant
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun ApiIslandSubPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }
    val capsuleManager = remember { CapsuleManager(context) }

    var isEnabled by remember { mutableStateOf(prefs.isApiIslandEnabled) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(top = 104.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                                imageVector = CupertinoIcons.Default.ChevronLeftForwardslashChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF5856D6),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Webhook & Broadcast API",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1C1917)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gửi thông báo tùy biến từ Tasker, MacroDroid, Python script hoặc Server nội bộ trực tiếp lên viên thuốc Magic Capsule của Honor.",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                        )
                    }
                }
            }

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
                            title = "Bật nhận API / Webhook",
                            subtitle = "Lắng nghe broadcast intent và local webhook",
                            selected = isEnabled,
                            onSelect = {
                                isEnabled = it
                                prefs.isApiIslandEnabled = it
                            },
                            isDark = isDark
                        )
                    }
                }
            }

            // Code snippet guide
            item {
                val codeBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF5F5F4)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(codeBg)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Ví dụ gửi qua ADB / Broadcast:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1C1917))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "am broadcast -a com.buwin.magiccapsule.action.PUSH_CAPSULE \\\n  --es capsule_title \"Server Ổn Định\" \\\n  --es title \"Homelab Server\" \\\n  --es message \"CPU 24% • RAM 4.2GB\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF64B5F6) else Color(0xFF007AFF)
                        )
                    }
                }
            }

            // Test Button
            item {
                LiquidButton(
                    onClick = {
                        capsuleManager.postDeviceStatsCapsule(
                            capsuleText = "🌐 Webhook: Server OK",
                            title = "Homelab Server Status",
                            details = "Uptime 14 ngày • Mạng 1.2 Gbps • Mọi dịch vụ hoạt động tốt"
                        )
                    },
                    variant = LiquidButtonVariant.Surface,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    isDark = isDark
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = CupertinoIcons.Filled.Paperplane, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF5856D6))
                        Text("Gửi thông báo API thử nghiệm lên Capsule", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    text = "API & Webhook",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            }
        )
    }
}
