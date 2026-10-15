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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
fun CustomCardSubPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val rootBackdrop = rememberLayerBackdrop()
    val prefs = remember { AppFeaturePreferences(context) }
    val capsuleManager = remember { CapsuleManager(context) }

    var isPermanent by remember { mutableStateOf(prefs.isCustomCardPermanent) }
    var cardText by remember { mutableStateOf(prefs.customCardText) }

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
                            Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = Color(0xFFFF9500), modifier = Modifier.size(20.dp))
                            Text("Đảo thường trực (Ghim cố định)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1C1917))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ghim một dòng thông điệp, châm ngôn hoặc ghi chú quan trọng cố định 24/7 trên viên thuốc Magic Capsule ở mép camera.",
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
                            title = "Bật đảo thường trực 24/7",
                            subtitle = "Luôn hiển thị thẻ tùy chỉnh ngay cả khi không có sự kiện",
                            selected = isPermanent,
                            onSelect = {
                                isPermanent = it
                                prefs.isCustomCardPermanent = it
                                if (it) {
                                    capsuleManager.postDeviceStatsCapsule(
                                        capsuleText = cardText.take(24),
                                        title = "Đảo thường trực",
                                        details = cardText
                                    )
                                } else {
                                    capsuleManager.dismiss(CapsuleManager.ID_DEVICE)
                                }
                            },
                            isDark = isDark
                        )

                        Text("Nội dung hiển thị trên đảo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF1C1917))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF202024) else Color(0xFFF5F5F7))
                                .padding(12.dp)
                        ) {
                            BasicTextField(
                                value = cardText,
                                onValueChange = {
                                    cardText = it
                                    prefs.customCardText = it
                                    if (isPermanent) {
                                        capsuleManager.postDeviceStatsCapsule(
                                            capsuleText = it.take(24),
                                            title = "Đảo thường trực",
                                            details = it
                                        )
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else Color(0xFF1C1917)
                                ),
                                cursorBrush = SolidColor(Color(0xFF007AFF)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                LiquidButton(
                    onClick = {
                        capsuleManager.postDeviceStatsCapsule(
                            capsuleText = cardText.take(24),
                            title = "Đảo thường trực",
                            details = cardText
                        )
                    },
                    variant = LiquidButtonVariant.Surface,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    isDark = isDark
                ) {
                    Text("Cập nhật ngay lên Magic Capsule", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

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
                    text = "Đảo thường trực",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (isDark) Color.White else Color(0xFF1C1917)
                )
            }
        )
    }
}
