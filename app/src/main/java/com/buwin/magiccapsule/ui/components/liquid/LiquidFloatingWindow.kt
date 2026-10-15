package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.ArrowDownCircle
import io.github.alexzhirkevich.cupertino.icons.filled.Bolt
import io.github.alexzhirkevich.cupertino.icons.filled.PlayCircle
import io.github.alexzhirkevich.cupertino.icons.filled.XmarkCircle

/**
 * Liquid Glass Mini Floating Window.
 *
 * Implements PiP / mini window pattern:
 * - Rounded corner morphing glass capsule
 * - Deep optical lens refraction with edge highlights
 * - Real-time stats, live status pill, and tactile controls
 */
@Composable
fun LiquidFloatingWindow(
    visible: Boolean,
    onClose: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalIsDark.current
) {
    val isLightTheme = !isDark
    val containerColor = if (isLightTheme) Color.White.copy(alpha = 0.80f) else Color(0xFF1E1E24).copy(alpha = 0.76f)
    val contentColor = if (isLightTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val accentColor = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(dampingRatio = 0.80f, stiffness = 350f)) +
                slideInVertically(initialOffsetY = { it / 2 }) +
                scaleIn(initialScale = 0.90f),
        exit = fadeOut(animationSpec = spring(dampingRatio = 0.80f, stiffness = 350f)) +
                slideOutVertically(targetOffsetY = { it / 2 }) +
                scaleOut(targetScale = 0.90f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(26f.dp) },
                    effects = {
                        vibrancy()
                        blur(if (isLightTheme) 14f.dp.toPx() else 10f.dp.toPx())
                        lens(
                            refractionHeight = 16f.dp.toPx(),
                            refractionAmount = 20f.dp.toPx(),
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    },
                    highlight = { Highlight.Default },
                    shadow = {
                        Shadow(
                            radius = 20f.dp,
                            color = if (isLightTheme) Color.Black.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.45f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 8f.dp,
                            alpha = if (isLightTheme) 0.08f else 0.20f
                        )
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .padding(18.dp)
        ) {
            // Header: Live Indicator Pill + Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing Green Live Status Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )

                    BasicText(
                        text = "Cửa sổ nổi Mini",
                        style = TextStyle(
                            color = contentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )

                    // Mode Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(Capsule())
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        BasicText(
                            text = "Liquid Glass",
                            style = TextStyle(
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.08f))
                        .clickable(role = Role.Button) { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Filled.XmarkCircle,
                        contentDescription = "Đóng",
                        tint = contentColor.copy(alpha = 0.60f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Performance & Download Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(contentColor.copy(alpha = 0.04f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CupertinoIcons.Filled.Bolt,
                            contentDescription = "Tốc độ",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        BasicText(
                            text = "Tải video chạy ngầm",
                            style = TextStyle(
                                color = contentColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        BasicText(
                            text = "Tối ưu hóa đa luồng & giữ nguyên âm thanh",
                            style = TextStyle(
                                color = contentColor.copy(alpha = 0.55f),
                                fontSize = 11.5.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(Capsule())
                        .background(Color(0xFF34C759).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    BasicText(
                        text = "Sẵn sàng",
                        style = TextStyle(
                            color = Color(0xFF34C759),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini stats metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatPill(
                    title = "Độ nét",
                    value = "HD 1080p",
                    icon = CupertinoIcons.Filled.PlayCircle,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    contentColor = contentColor
                )
                MiniStatPill(
                    title = "Logo Watermark",
                    value = "Đã loại bỏ",
                    icon = CupertinoIcons.Filled.ArrowDownCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun MiniStatPill(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    contentColor: Color
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Column {
            BasicText(
                text = title,
                style = TextStyle(
                    color = contentColor.copy(alpha = 0.50f),
                    fontSize = 10.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            BasicText(
                text = value,
                style = TextStyle(
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}
