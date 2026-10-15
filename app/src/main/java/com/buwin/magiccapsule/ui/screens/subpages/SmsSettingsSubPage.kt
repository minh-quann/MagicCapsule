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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.buwin.magiccapsule.data.preferences.AppFeaturePreferences
import com.buwin.magiccapsule.ui.components.liquid.LiquidButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidButtonVariant
import com.buwin.magiccapsule.ui.components.liquid.LiquidRoundButton
import com.buwin.magiccapsule.ui.components.liquid.LiquidSlider
import com.buwin.magiccapsule.ui.components.liquid.LiquidTopBar
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.cardBorderColor
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun SmsSettingsSubPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }
    val capsuleManager = remember { CapsuleManager(context) }

    var isEnabled by remember { mutableStateOf(prefs.isSmsIslandEnabled) }
    var autoCopy by remember { mutableStateOf(prefs.autoCopyOtp) }
    var durationSec by remember { mutableFloatStateOf(prefs.smsCapsuleDurationSec.toFloat()) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop),
            contentPadding = PaddingValues(top = 104.dp, bottom = 48.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Feature Description Card
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
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = Color(0xFFFF9500),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Bắt mã OTP & Bưu kiện thông minh",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1C1917)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Khi có tin nhắn chứa mã OTP hoặc mã nhận hàng bưu tá, Magic Capsule sẽ hiện nổi bật ở đỉnh màn hình kèm nút sao chép 1 chạm cực kỳ tiện lợi.",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFFA8A29E) else Color(0xFF78716C)
                        )
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
                            title = "Bật Đảo mã xác minh SMS",
                            subtitle = "Tự động trích xuất mã OTP và mã lấy hàng",
                            selected = isEnabled,
                            onSelect = {
                                isEnabled = it
                                prefs.isSmsIslandEnabled = it
                            },
                            isDark = isDark
                        )

                        SubpageToggleRow(
                            title = "Sao chép mã 1 chạm",
                            subtitle = "Bấm trực tiếp vào Capsule để chép mã vào bộ nhớ tạm",
                            selected = autoCopy,
                            onSelect = {
                                autoCopy = it
                                prefs.autoCopyOtp = it
                            },
                            isDark = isDark
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Thời gian duy trì viên thuốc", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF1C1917))
                                Text("${durationSec.toInt()} giây", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9500))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LiquidSlider(
                                value = { durationSec },
                                onValueChange = {
                                    durationSec = it
                                    prefs.smsCapsuleDurationSec = it.toInt()
                                },
                                valueRange = 5f..30f,
                                visibilityThreshold = 1f,
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
                        capsuleManager.postSmsOtpCapsule(
                            code = "829415",
                            typeLabel = "Mã xác minh Vietcombank",
                            sender = "VCB_OTP"
                        )
                    },
                    variant = LiquidButtonVariant.Surface,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    isDark = isDark
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFF9500))
                        Text("Gửi giả lập mã OTP (829415) lên Magic Capsule", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", modifier = Modifier.size(18.dp))
                }
            },
            title = {
                Text(
                    text = "Mã xác minh & Bưu kiện",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            }
        )
    }
}
