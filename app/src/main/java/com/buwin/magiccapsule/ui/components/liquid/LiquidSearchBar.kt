package com.buwin.magiccapsule.ui.components.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.XmarkCircle
import io.github.alexzhirkevich.cupertino.icons.outlined.ArrowDownToLine
import io.github.alexzhirkevich.cupertino.icons.outlined.Clipboard
import io.github.alexzhirkevich.cupertino.icons.outlined.MagnifyingGlass

/**
 * Liquid Glass Search Bar.
 *
 * Implements search bar pattern:
 * - Fluid spring transition: Search bar stretches across full width and smoothly resizes to accommodate
 *   the detached glass "Hủy" (Cancel) button sliding in from the trailing edge.
 * - Synchronized expandHorizontally + slideInHorizontally for smooth animation.
 * - Real-time optical refraction, vibrancy, and specular highlights.
 */
@Composable
fun LiquidSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    placeholder: String = "Dán link TikTok tại đây...",
    isLoading: Boolean = false,
    onSearch: () -> Unit = {},
    onPaste: (() -> Unit)? = null,
    isDark: Boolean = LocalIsDark.current
) {
    val isLightTheme = !isDark
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Active editing state (true when focused or containing text)
    var isEditing by remember { mutableStateOf(value.isNotEmpty()) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.isNotEmpty() && !isEditing) {
            isEditing = true
        }
    }

    // Interaction sources for physical touch feedback
    val searchBarInteractionSource = remember { MutableInteractionSource() }
    val isPressed by searchBarInteractionSource.collectIsPressedAsState()

    val pasteInteractionSource = remember { MutableInteractionSource() }
    val isPastePressed by pasteInteractionSource.collectIsPressedAsState()

    val downloadInteractionSource = remember { MutableInteractionSource() }
    val isDownloadPressed by downloadInteractionSource.collectIsPressedAsState()

    val accentColor = if (isLightTheme) Color(0xFF007AFF) else Color(0xFF0A84FF)
    val contentColor = if (isLightTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val containerColor = if (isLightTheme) Color.White else Color(0xFF1C1C1E)
    val searchBorderColor = if (isLightTheme) Color(0xFFE6E8EC) else Color(0xFF2C2C2E)

    // Apple iOS 26 fluid spring physics (mass: 1.0, stiffness: 350.0, damping: 30.0 -> dampingRatio: 0.80f)
    val springFloatSpec = spring<Float>(dampingRatio = 0.80f, stiffness = 350f)
    val springIntSizeSpec = spring<IntSize>(dampingRatio = 0.80f, stiffness = 350f)
    val springIntOffsetSpec = spring<IntOffset>(dampingRatio = 0.80f, stiffness = 350f)

    // Tactile press scales
    val searchScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = springFloatSpec,
        label = "searchBarScale"
    )

    val pasteScale by animateFloatAsState(
        targetValue = if (isPastePressed) 0.93f else 1f,
        animationSpec = springFloatSpec,
        label = "pasteScale"
    )

    val downloadScale by animateFloatAsState(
        targetValue = if (isDownloadPressed) 0.92f else 1f,
        animationSpec = springFloatSpec,
        label = "downloadScale"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 1. Main Search Field (Matches card background and subtle border with zero blue tint) ──
        Row(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .graphicsLayer {
                    scaleX = searchScale
                    scaleY = searchScale
                }
                .clip(RoundedCornerShape(25.dp))
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = searchBorderColor,
                    shape = RoundedCornerShape(25.dp)
                )
                .clickable(
                    interactionSource = searchBarInteractionSource,
                    indication = null
                ) {
                    isEditing = true
                    try {
                        focusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
                .padding(start = 14.dp, end = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Magnifying Glass Glyph
            Icon(
                imageVector = CupertinoIcons.Default.MagnifyingGlass,
                contentDescription = "Tìm kiếm",
                tint = contentColor.copy(alpha = 0.50f),
                modifier = Modifier.size(20.dp)
            )

            // Interactive Text Field (always present, never destroyed/recreated)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (focusState.isFocused) {
                            isEditing = true
                        } else if (value.isEmpty()) {
                            isEditing = false
                        }
                    },
                singleLine = true,
                textStyle = TextStyle(
                    color = contentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                cursorBrush = SolidColor(accentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            BasicText(
                                text = placeholder,
                                style = TextStyle(
                                    color = contentColor.copy(alpha = 0.45f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Trailing Quick Actions
            // Quick Paste Action (Apple-style minimalist glass circle icon button, shown when field is empty)
            AnimatedVisibility(
                visible = value.isEmpty() && onPaste != null,
                enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.85f)
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = pasteScale
                            scaleY = pasteScale
                        }
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLightTheme) Color.Black.copy(alpha = 0.05f)
                            else Color.White.copy(alpha = 0.09f)
                        )
                        .border(
                            width = 0.75.dp,
                            color = if (isLightTheme) Color.Black.copy(alpha = 0.08f)
                            else Color.White.copy(alpha = 0.16f),
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = pasteInteractionSource,
                            indication = null,
                            role = Role.Button
                        ) {
                            onPaste?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Outlined.Clipboard,
                        contentDescription = "Dán từ bảng nhớ tạm",
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Quick Clear Button (shown when field has text)
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.85f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CupertinoIcons.Filled.XmarkCircle,
                        contentDescription = "Xóa nội dung",
                        tint = contentColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── 2. Detached Liquid Glass Download Action Pill (Replaces Cancel button) ──
        AnimatedVisibility(
            visible = value.isNotEmpty() || isEditing,
            enter = expandHorizontally(
                animationSpec = springIntSizeSpec,
                expandFrom = Alignment.End
            ) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = springIntOffsetSpec
            ) + fadeIn(animationSpec = tween(180)),
            exit = shrinkHorizontally(
                animationSpec = springIntSizeSpec,
                shrinkTowards = Alignment.End
            ) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = springIntOffsetSpec
            ) + fadeOut(animationSpec = tween(140))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))

                val isActionActive = value.isNotEmpty() || isLoading
                val buttonBgColor = if (isActionActive) accentColor else containerColor
                val buttonBorderColor = if (isActionActive) Color.Transparent else searchBorderColor
                val buttonTextColor = if (isActionActive) Color.White else contentColor.copy(alpha = 0.50f)

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .graphicsLayer {
                            scaleX = downloadScale
                            scaleY = downloadScale
                        }
                        .clip(RoundedCornerShape(25.dp))
                        .background(buttonBgColor)
                        .border(
                            width = 1.dp,
                            color = buttonBorderColor,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .clickable(
                            interactionSource = downloadInteractionSource,
                            indication = null,
                            enabled = !isLoading && isActionActive,
                            role = Role.Button
                        ) {
                            focusManager.clearFocus()
                            if (value.isNotEmpty()) {
                                onSearch()
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = buttonTextColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = CupertinoIcons.Outlined.ArrowDownToLine,
                                contentDescription = "Tải xuống",
                                tint = buttonTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        BasicText(
                            text = if (isLoading) "Đang tải..." else "Tải xuống",
                            style = TextStyle(
                                color = buttonTextColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }
        }
    }
}
