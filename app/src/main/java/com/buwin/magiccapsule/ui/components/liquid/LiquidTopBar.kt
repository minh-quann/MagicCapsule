package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buwin.magiccapsule.ui.theme.BackgroundDark
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.kyant.backdrop.Backdrop

/**
 * Shared iOS Liquid Glass Progressive Blur Top Bar.
 * Produces an alpha-masked progressive blur that smoothly dissolves
 * scrolling content into transparency at the bottom edge.
 */
@Composable
fun LiquidTopBar(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalIsDark.current,
    headerHeight: Dp = 114.dp,
    tintColor: Color = Color.Unspecified,
    tintIntensity: Float = 0.8f,
    blurRadius: Dp = 6.dp,
    fadeStartRatio: Float = 0.5f,
    navigationIcon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val isLightTheme = !isDark
    val effectiveTintColor = if (tintColor.isSpecified) {
        tintColor
    } else {
        if (isLightTheme) Color.White else BackgroundDark
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // Progressive Blur Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alphaMaskedProgressiveBlur(
                    backdrop = backdrop,
                    tint = effectiveTintColor,
                    tintIntensity = tintIntensity,
                    blurRadius = blurRadius,
                    fadeStartRatio = fadeStartRatio,
                    direction = ProgressiveBlurDirection.TopToBottom
                )
        )

        // Interactive Header Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    navigationIcon?.invoke()
                    title()
                }

                if (actions != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        content = actions
                    )
                }
            }
            bottomContent?.invoke()
        }
    }
}

/**
 * Convenient shared overload with structured text title, optional subtitle and badge.
 */
@Composable
fun LiquidTopBar(
    backdrop: Backdrop,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isDark: Boolean = LocalIsDark.current,
    headerHeight: Dp = 114.dp,
    tintColor: Color = Color.Unspecified,
    tintIntensity: Float = 0.8f,
    blurRadius: Dp = 6.dp,
    fadeStartRatio: Float = 0.5f,
    navigationIcon: (@Composable () -> Unit)? = null,
    titleBadge: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null
) {
    LiquidTopBar(
        backdrop = backdrop,
        modifier = modifier,
        isDark = isDark,
        headerHeight = headerHeight,
        tintColor = tintColor,
        tintIntensity = tintIntensity,
        blurRadius = blurRadius,
        fadeStartRatio = fadeStartRatio,
        navigationIcon = navigationIcon,
        bottomContent = bottomContent,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.2.sp
                    )
                    titleBadge?.invoke()
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1C1C1E).copy(alpha = 0.75f)
                    )
                }
            }
        },
        actions = actions
    )
}
