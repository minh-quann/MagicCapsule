package com.buwin.magiccapsule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.buwin.magiccapsule.R

/**
 * Google Sans Flex font family matching MEBIECO mobile design typography.
 * Supports full range of font weights from Thin (100) to Black (900).
 */
val GoogleSansFlexFamily = FontFamily(
    Font(R.font.google_sans_flex_thin, FontWeight.Thin),
    Font(R.font.google_sans_flex_extra_light, FontWeight.ExtraLight),
    Font(R.font.google_sans_flex_light, FontWeight.Light),
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_semi_bold, FontWeight.SemiBold),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
    Font(R.font.google_sans_flex_extra_bold, FontWeight.ExtraBold),
    Font(R.font.google_sans_flex_black, FontWeight.Black)
)

private val defaultTypography = Typography()

/**
 * Global Material 3 Typography using Google Sans Flex across all text hierarchies.
 */
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = GoogleSansFlexFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = GoogleSansFlexFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = GoogleSansFlexFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = GoogleSansFlexFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = GoogleSansFlexFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = GoogleSansFlexFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = GoogleSansFlexFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = GoogleSansFlexFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = GoogleSansFlexFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = GoogleSansFlexFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = GoogleSansFlexFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = GoogleSansFlexFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = GoogleSansFlexFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = GoogleSansFlexFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = GoogleSansFlexFamily)
)