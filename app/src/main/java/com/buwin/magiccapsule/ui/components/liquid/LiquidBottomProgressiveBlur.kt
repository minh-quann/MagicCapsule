package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.buwin.magiccapsule.ui.theme.BackgroundDark
import com.buwin.magiccapsule.ui.theme.BackgroundLight
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.kyant.backdrop.Backdrop

/**
 * Progressive Blur Footer dissolving scrolling content towards bottom edge.
 */
@Composable
fun LiquidBottomProgressiveBlur(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    isDark: Boolean = LocalIsDark.current,
    footerHeight: Dp = 100.dp,
    tintColor: Color = Color.Unspecified,
    tintIntensity: Float = 0.8f,
    blurRadius: Dp = 6.dp,
    fadeStartRatio: Float = 0.5f
) {
    val isLightTheme = !isDark
    val effectiveTintColor = if (tintColor.isSpecified) {
        tintColor
    } else {
        if (isLightTheme) Color.White else BackgroundDark
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val effectiveHeight = remember(footerHeight, navBarBottom) {
        maxOf(footerHeight, 80.dp + navBarBottom)
    }

    if (backdrop != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(effectiveHeight)
                .alphaMaskedProgressiveBlur(
                    backdrop = backdrop,
                    tint = effectiveTintColor,
                    tintIntensity = tintIntensity,
                    blurRadius = blurRadius,
                    fadeStartRatio = fadeStartRatio,
                    direction = ProgressiveBlurDirection.BottomToTop
                )
        )
    } else {
        // Fallback gradient if no backdrop provided
        val fadeBaseColor = if (isDark) BackgroundDark else BackgroundLight
        val fadeBrush = remember(fadeBaseColor) {
            Brush.verticalGradient(
                0.00f to Color.Transparent,
                0.30f to fadeBaseColor.copy(alpha = 0.15f),
                0.60f to fadeBaseColor.copy(alpha = 0.55f),
                1.00f to fadeBaseColor.copy(alpha = 0.90f)
            )
        }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(effectiveHeight)
                .background(fadeBrush)
        )
    }
}
