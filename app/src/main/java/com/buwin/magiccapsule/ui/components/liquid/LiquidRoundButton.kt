package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Circular / Round button with Liquid Glass refraction, blur, specular highlight, and tactile deformation.
 * Supports 3 variants:
 * - Transparent: pure glass refraction without surface overlay
 * - Surface: frosted translucent surface with light/dark adaptive tint
 * - Tinted: Hue-blended vibrant color glass
 */
@Composable
fun LiquidRoundButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    variant: LiquidButtonVariant = LiquidButtonVariant.Surface,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    blurRadius: Dp = 4.dp,
    lensHeight: Dp = 16.dp,
    lensWidth: Dp = 24.dp,
    content: @Composable () -> Unit
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
        InteractiveHighlight(animationScope = animationScope)
    }

    Box(
        modifier = modifier
            .size(size)
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
                        val progress = interactiveHighlight.pressProgress
                        val offset = interactiveHighlight.offset
                        val dragDist = hypot(offset.x, offset.y)
                        val offsetAngle = atan2(offset.y, offset.x)

                        // Apple Fluid Interface: Damped rubber-band displacement
                        val maxDisplacement = (this.size.minDimension * 0.18f).coerceAtMost(8f.dp.toPx())
                        val dampingDistance = (this.size.minDimension * 1.5f).coerceAtLeast(30f.dp.toPx())
                        val dampedDistance = maxDisplacement * tanh(dragDist / dampingDistance)

                        translationX = dampedDistance * cos(offsetAngle)
                        translationY = dampedDistance * sin(offsetAngle)

                        // Apple Liquid Glass tactile deformation: Volume-preserving squash & stretch
                        val initialScale = 1f
                        val pressedScale = 1f + (4f.dp.toPx() / this.size.height)
                        val pressScale = lerp(initialScale, pressedScale, progress)

                        val normalizedDist = (dragDist / dampingDistance).coerceIn(0f, 1f)
                        val stretchFactor = 1f + 0.35f * normalizedDist
                        val squashFactor = 1f / sqrt(stretchFactor)

                        val scaleAlong = stretchFactor
                        val scalePerp = squashFactor

                        val cos2 = cos(offsetAngle) * cos(offsetAngle)
                        val sin2 = sin(offsetAngle) * sin(offsetAngle)
                        scaleX = pressScale * (scaleAlong * cos2 + scalePerp * sin2)
                        scaleY = pressScale * (scaleAlong * sin2 + scalePerp * cos2)
                    }
                } else null,
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
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides effectiveContentColor) {
            content()
        }
    }
}

/**
 * Convenience builder for a 100% Transparent Liquid Round Button with pure refraction and specular highlight.
 */
@Composable
fun TransparentLiquidRoundButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,
    contentColor: Color = Color.Unspecified,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    content: @Composable () -> Unit
) {
    LiquidRoundButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        variant = LiquidButtonVariant.Transparent,
        size = size,
        shape = shape,
        contentColor = contentColor,
        isInteractive = isInteractive,
        showBorder = showBorder,
        isDark = isDark,
        content = content
    )
}

/**
 * Convenience builder for a Surface Frosted Liquid Round Button.
 */
@Composable
fun SurfaceLiquidRoundButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,
    surfaceColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    content: @Composable () -> Unit
) {
    LiquidRoundButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        variant = LiquidButtonVariant.Surface,
        size = size,
        shape = shape,
        surfaceColor = surfaceColor,
        contentColor = contentColor,
        isInteractive = isInteractive,
        showBorder = showBorder,
        isDark = isDark,
        content = content
    )
}

/**
 * Convenience builder for a Tinted Liquid Round Button (e.g. Apple blue, orange, green).
 */
@Composable
fun TintedLiquidRoundButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,
    contentColor: Color = Color.White,
    isInteractive: Boolean = true,
    showBorder: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    content: @Composable () -> Unit
) {
    LiquidRoundButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        variant = LiquidButtonVariant.Tinted,
        tint = tint,
        size = size,
        shape = shape,
        contentColor = contentColor,
        isInteractive = isInteractive,
        showBorder = showBorder,
        isDark = isDark,
        content = content
    )
}
