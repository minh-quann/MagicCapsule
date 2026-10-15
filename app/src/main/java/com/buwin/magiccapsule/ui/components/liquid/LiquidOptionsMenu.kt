package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import io.github.alexzhirkevich.cupertino.icons.outlined.Checkmark

/**
 * Data class representing an item inside the Liquid Glass Options Menu.
 */
data class LiquidMenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val iconTint: Color? = null,
    val isSelected: Boolean = false,
    val onClick: () -> Unit
)

/**
 * Liquid Glass Options Menu.
 *
 * Implements context menu aesthetics:
 * - Rounded corner morphing glass capsule
 * - Deep optical lens refraction with edge highlights
 * - Fluid spring animation on open / close
 */
@Composable
fun LiquidOptionsMenu(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    items: List<LiquidMenuItem>,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalIsDark.current
) {
    val isLightTheme = !isDark
    val containerColor = if (isLightTheme) Color.White.copy(alpha = 0.78f) else Color(0xFF1E1E24).copy(alpha = 0.75f)
    val contentColor = if (isLightTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val accentColor = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
    val dimColor = if (isLightTheme) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.55f)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) +
                scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)),
        exit = fadeOut(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) +
                scaleOut(targetScale = 0.88f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = 24.dp)
                    .width(320.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent outside dismissal when clicking inside
                    )
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
                                radius = 24f.dp,
                                color = if (isLightTheme) Color.Black.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.50f)
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
                    .padding(16.dp)
            ) {
                // Menu Header Label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicText(
                        text = title.uppercase(),
                        style = TextStyle(
                            color = contentColor.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )

                    // Close Glass Pill
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.08f))
                            .clickable(role = Role.Button) { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(
                            text = "✕",
                            style = TextStyle(
                                color = contentColor.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Menu Options List
                items.forEachIndexed { index, item ->
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    val isItemPressed by itemInteractionSource.collectIsPressedAsState()
                    val itemScale by animateFloatAsState(
                        targetValue = if (isItemPressed) 0.96f else 1f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                        label = "menuItemScale"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = itemScale
                                scaleY = itemScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (item.isSelected) accentColor.copy(alpha = if (isLightTheme) 0.12f else 0.22f)
                                else if (isItemPressed) contentColor.copy(alpha = 0.06f)
                                else Color.Transparent
                            )
                            .clickable(
                                interactionSource = itemInteractionSource,
                                indication = null,
                                role = Role.Button
                            ) {
                                item.onClick()
                                onDismissRequest()
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item.icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background((item.iconTint ?: accentColor).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconTint ?: accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = item.title,
                                style = TextStyle(
                                    color = if (item.isSelected) accentColor else contentColor,
                                    fontSize = 15.sp,
                                    fontWeight = if (item.isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            if (item.subtitle != null) {
                                BasicText(
                                    text = item.subtitle,
                                    style = TextStyle(
                                        color = contentColor.copy(alpha = 0.55f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                        }

                        if (item.isSelected) {
                            Icon(
                                imageVector = CupertinoIcons.Default.Checkmark,
                                contentDescription = "Đã chọn",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = contentColor.copy(alpha = 0.06f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}
