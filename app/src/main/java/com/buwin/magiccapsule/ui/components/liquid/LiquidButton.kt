package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Visual styling variants for liquid glass button:
 * - [Transparent]: Pure glass refraction and specular highlight without surface overlay.
 * - [Surface]: Frosted glass with adaptive translucent surface tint (or custom surfaceColor).
 * - [Tinted]: Rich color saturation wash using Hue blending (e.g. Apple blue, orange, yellow).
 */
enum class LiquidButtonVariant {
    Transparent,
    Surface,
    Tinted
}

/**
 * Pill / Capsule button with Liquid Glass refraction, blur, specular highlight, and tactile deformation.
 * Supports 3 variants:
 * - Transparent: pure glass refraction without surface overlay
 * - Surface: frosted translucent surface with light/dark adaptive tint
 * - Tinted: Hue-blended vibrant color glass
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    variant: LiquidButtonVariant = LiquidButtonVariant.Surface,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    shape: Shape = Capsule(),
    blurRadius: Dp = 4.dp,
    lensHeight: Dp = 12.dp,
    lensWidth: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    // Resolve variant safely inside function body to avoid uninitialized default-arg forward reference
    val resolvedVariant = if (tint.isSpecified && variant == LiquidButtonVariant.Surface) {
        LiquidButtonVariant.Tinted
    } else {
        variant
    }

    val isLightTheme = !isDark
    val containerColor = if (surfaceColor.isSpecified) {
        surfaceColor
    } else {
        if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = 0.35f)
        else Color(0xFF505056).copy(alpha = 0.55f)
    }

    val effectiveContentColor = if (contentColor.isSpecified) {
        contentColor
    } else {
        when (resolvedVariant) {
            LiquidButtonVariant.Tinted -> Color.White
            LiquidButtonVariant.Transparent,
            LiquidButtonVariant.Surface -> if (isDark) Color.White else Color.Black
        }
    }

    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    Row(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(blurRadius.toPx())
                    lens(lensHeight.toPx(), lensWidth.toPx())
                },
                highlight = {
                    if (showBorder) {
                        Highlight.Plain
                    } else {
                        Highlight.Default.copy(alpha = if (isLightTheme) 0.55f else 0.35f)
                    }
                },
                shadow = {
                    Shadow(
                        radius = 8f.dp,
                        color = if (isLightTheme) Color.Black.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.35f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 6f.dp,
                        alpha = if (isLightTheme) 0.08f else 0.18f
                    )
                },
                layerBlock = if (isInteractive) {
                    {
                        val width = size.width
                        val height = size.height

                        val progress = interactiveHighlight.pressProgress
                        val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                        val maxOffset = size.minDimension
                        val initialDerivative = 0.05f
                        val offset = interactiveHighlight.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                        val maxDragScale = 4f.dp.toPx() / size.height
                        val offsetAngle = atan2(offset.y, offset.x)
                        scaleX =
                            scale +
                                    maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                    (width / height).fastCoerceAtMost(1f)
                        scaleY =
                            scale +
                                    maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                    (height / width).fastCoerceAtMost(1f)
                    }
                } else {
                    null
                },
                onDrawSurface = {
                    when (resolvedVariant) {
                        LiquidButtonVariant.Transparent -> {
                            // Pure transparent liquid glass - no surface color drawn
                        }
                        LiquidButtonVariant.Surface -> {
                            drawRect(containerColor)
                        }
                        LiquidButtonVariant.Tinted -> {
                            val effectiveTint = if (tint.isSpecified) tint else Color(0xFF007AFF)
                            drawRect(effectiveTint, blendMode = BlendMode.Hue)
                            drawRect(effectiveTint.copy(alpha = 0.75f))
                            if (surfaceColor.isSpecified) {
                                drawRect(surfaceColor)
                            }
                        }
                    }
                }
            )
            .clickable(
                interactionSource = null,
                indication = if (isInteractive) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (isInteractive) {
                    Modifier
                        .then(interactiveHighlight.modifier)
                        .then(interactiveHighlight.gestureModifier)
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8f.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides effectiveContentColor) {
            content()
        }
    }
}

/**
 * Convenience builder for a 100% Transparent Liquid Button with pure refraction and specular highlight.
 */
@Composable
fun TransparentLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Unspecified,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    shape: Shape = Capsule(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        variant = LiquidButtonVariant.Transparent,
        contentColor = contentColor,
        isInteractive = isInteractive,
        showBorder = showBorder,
        isDark = isDark,
        shape = shape,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Convenience builder for a Surface Frosted Liquid Button.
 */
@Composable
fun SurfaceLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    surfaceColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    shape: Shape = Capsule(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        variant = LiquidButtonVariant.Surface,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        isInteractive = isInteractive,
        showBorder = showBorder,
        isDark = isDark,
        shape = shape,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Convenience builder for a Tinted Liquid Button (e.g., Apple blue, orange, green).
 */
@Composable
fun TintedLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    tint: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    shape: Shape = Capsule(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        variant = LiquidButtonVariant.Tinted,
        tint = tint,
        contentColor = contentColor,
        isInteractive = isInteractive,
        showBorder = showBorder,
        isDark = isDark,
        shape = shape,
        contentPadding = contentPadding,
        content = content
    )
}

