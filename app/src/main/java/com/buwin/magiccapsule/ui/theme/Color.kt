package com.buwin.magiccapsule.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Accents
val PrimaryBlue = Color(0xFF007AFF)
val PrimaryBlueDark = Color(0xFF0A84FF)

// Card Borders
val CardBorderColor = Color(0xFFE6E8EC)
val CardBorderLight = Color(0xFFE6E8EC)
val CardBorderDark = Color(0xFF2C2C2E)

/**
 * Resolves standard card border color based on theme mode.
 */
fun cardBorderColor(isDark: Boolean): Color = if (isDark) CardBorderDark else CardBorderLight

// Light Palette (Warm neutral / Stone)
val BackgroundLight = Color(0xFFFAFAF9) 
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF5F5F4)
val TextPrimaryLight = Color(0xFF1C1917)
val TextSecondaryLight = Color(0xFF78716C)
val OutlineLight = Color(0xFFE6E8EC)

// Dark Palette (Pure OLED Black)
val BackgroundDark = Color(0xFF000000) // Pure Black as requested
val SurfaceDark = Color(0xFF121212)
val SurfaceVariantDark = Color(0xFF18181B)
val TextPrimaryDark = Color(0xFFF5F5F4)
val TextSecondaryDark = Color(0xFFA8A29E)
val OutlineDark = Color(0xFF27272A)