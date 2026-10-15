package com.buwin.magiccapsule.ui.components.liquid

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect

/**
 * Direction of the progressive blur transition.
 */
enum class ProgressiveBlurDirection {
    /** Blurred and tinted at the top, dissolving smoothly into transparency at the bottom (Top Bars / Headers). */
    TopToBottom,

    /** Transparent at the top, smoothly becoming blurred and tinted at the bottom (Bottom Bars / Footers). */
    BottomToTop
}

/**
 * Alpha-Masked Progressive Blur modifier.
 * Uses drawPlainBackdrop with AGSL RuntimeShader to modulate blur and tint
 * intensity via smoothstep, seamlessly dissolving into transparent sharpness.
 */
fun Modifier.alphaMaskedProgressiveBlur(
    backdrop: Backdrop,
    shape: Shape = RectangleShape,
    blurRadius: Dp = 6.dp,
    tint: Color = Color.White,
    tintIntensity: Float = 0.8f,
    fadeStartRatio: Float = 0.5f,
    direction: ProgressiveBlurDirection = ProgressiveBlurDirection.TopToBottom
): Modifier = this.then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shaderCode = when (direction) {
            ProgressiveBlurDirection.TopToBottom -> """
uniform shader content;

uniform float2 size;
layout(color) uniform half4 tint;
uniform float tintIntensity;
uniform float fadeStartRatio;

half4 main(float2 coord) {
    float blurAlpha = smoothstep(size.y, size.y * fadeStartRatio, coord.y);
    float tintAlpha = smoothstep(size.y, size.y * fadeStartRatio, coord.y);
    return mix(content.eval(coord) * blurAlpha, tint * tintAlpha, tintIntensity);
}"""
            ProgressiveBlurDirection.BottomToTop -> """
uniform shader content;

uniform float2 size;
layout(color) uniform half4 tint;
uniform float tintIntensity;
uniform float fadeStartRatio;

half4 main(float2 coord) {
    float blurAlpha = smoothstep(0.0, size.y * (1.0 - fadeStartRatio), coord.y);
    float tintAlpha = smoothstep(0.0, size.y * (1.0 - fadeStartRatio), coord.y);
    return mix(content.eval(coord) * blurAlpha, tint * tintAlpha, tintIntensity);
}"""
        }

        val shaderKey = when (direction) {
            ProgressiveBlurDirection.TopToBottom -> "AlphaMaskTopToBottom"
            ProgressiveBlurDirection.BottomToTop -> "AlphaMaskBottomToTop"
        }

        Modifier.drawPlainBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                blur(blurRadius.toPx())
                runtimeShaderEffect(
                    shaderKey,
                    shaderCode,
                    "content"
                ) {
                    setFloatUniform("size", size.width, size.height)
                    setColorUniform("tint", tint)
                    setFloatUniform("tintIntensity", tintIntensity)
                    setFloatUniform("fadeStartRatio", fadeStartRatio)
                }
            }
        )
    } else {
        // Graceful fallback for Android < 13
        val gradientColors = when (direction) {
            ProgressiveBlurDirection.TopToBottom -> listOf(
                tint.copy(alpha = tintIntensity * 0.85f),
                tint.copy(alpha = tintIntensity * 0.40f),
                Color.Transparent
            )
            ProgressiveBlurDirection.BottomToTop -> listOf(
                Color.Transparent,
                tint.copy(alpha = tintIntensity * 0.40f),
                tint.copy(alpha = tintIntensity * 0.85f)
            )
        }
        Modifier
            .drawPlainBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    blur(blurRadius.toPx() * 1.5f)
                }
            )
            .background(Brush.verticalGradient(colors = gradientColors))
    }
)
