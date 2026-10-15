package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangle

/**
 * Liquid Glass Modal Dialog container with depth lens refraction and edge highlights.
 */
@Composable
fun LiquidModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isLightTheme = !LocalIsDark.current
    val dimColor = if (isLightTheme) Color(0xFF1E293B).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.65f)
    val containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = 0.65f) else Color(0xFF18181B).copy(alpha = 0.55f)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f)
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
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent dismiss when clicking inside the modal
                    )
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(cornerRadius) },
                        effects = {
                            colorControls(
                                brightness = if (isLightTheme) 0.15f else -0.05f,
                                saturation = 1.4f
                            )
                            blur(if (isLightTheme) 18f.dp.toPx() else 12f.dp.toPx())
                            lens(24f.dp.toPx(), 48f.dp.toPx(), depthEffect = true)
                        },
                        highlight = { Highlight.Plain },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .padding(24.dp),
                content = content
            )
        }
    }
}
