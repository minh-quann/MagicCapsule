package com.buwin.magiccapsule.ui.components.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.ExclamationmarkTriangle
import kotlinx.coroutines.launch

/**
 * Ultra-smooth iOS Spring Physics Floating Confirmation Modal Sheet.
 *
 * Characteristics:
 * - GPU RenderNode hardware translation for zero-jank 120 FPS animations.
 * - Physics-based iOS spring curve (dampingRatio = 0.82f, stiffness = 380f) with natural settle and bounce.
 * - Solid Apple-styled card container (no liquid glass on the card) with 16dp horizontal margin.
 * - Flat Liquid Glass action buttons placed horizontally side-by-side on a single row without shadows.
 * - Responsive swipe-down gesture with rubber-band elasticity and velocity tracking.
 * - Renders in a top-level overlay window above all navigation bars.
 */
@Composable
fun AppConfirmationModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Xác nhận",
    cancelText: String = "Hủy",
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    backdrop: Backdrop? = null,
    isDark: Boolean = LocalIsDark.current,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    // Controls Window composition
    var showDialog by remember { mutableStateOf(visible) }
    var isClosing by remember { mutableStateOf(false) }

    // GPU-driven animation states
    val animProgress = remember { Animatable(0f) }
    val dragOffsetY = remember { Animatable(0f) }
    var sheetHeightPx by remember { mutableFloatStateOf(800f) }
    val velocityTracker = remember { VelocityTracker() }

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val isLightTheme = !isDark

    // Spring animation spec matching Apple iOS 17/18 Presentation Sheet
    val sheetSpringSpec = remember {
        spring<Float>(
            dampingRatio = 0.82f,
            stiffness = 380f
        )
    }
    val buttonSpringSpec = remember {
        spring<Float>(
            dampingRatio = 0.80f,
            stiffness = 450f
        )
    }

    // Dismiss with physics-based exit animation
    fun dismissWithAnimation(onFinished: () -> Unit = onDismissRequest) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            launch {
                dragOffsetY.animateTo(
                    targetValue = sheetHeightPx,
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = 420f)
                )
            }
            animProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.90f, stiffness = 450f)
            )
            isClosing = false
            showDialog = false
            onFinished()
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            isClosing = false
            showDialog = true
            dragOffsetY.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = sheetSpringSpec
            )
        } else if (showDialog && !isClosing) {
            dismissWithAnimation()
        }
    }

    if (!showDialog) return

    val sheetShape = RoundedCornerShape(28.dp)
    val buttonShape = CircleShape

    // Palette tokens: Solid clean Apple surface for the card container
    val cardBgColor = if (isLightTheme) Color.White else Color(0xFF1C1C1E)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val dragHandleColor = if (isLightTheme) Color.Black.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.24f)

    val titleColor = if (isLightTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val subtitleColor = if (isLightTheme) Color(0xFF636366) else Color(0xFF8E8E93)

    val resolvedIcon = icon ?: if (isDestructive) CupertinoIcons.Outlined.ExclamationmarkTriangle else null
    val resolvedIconTint = iconTint ?: if (isDestructive) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary

    val cardModifier = Modifier
        .shadow(
            elevation = 4.dp,
            shape = sheetShape,
            spotColor = Color.Black.copy(alpha = if (isLightTheme) 0.06f else 0.20f),
            ambientColor = Color.Transparent
        )
        .clip(sheetShape)
        .background(cardBgColor)
        .border(BorderStroke(1.dp, cardBorder), sheetShape)

    Dialog(
        onDismissRequest = { dismissWithAnimation() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // GPU-rendered scrim opacity tied directly to animation progress and drag distance
                    val progress = animProgress.value
                    val dragY = dragOffsetY.value
                    val dragRatio = (dragY / sheetHeightPx).coerceIn(0f, 1f)
                    val maxAlpha = if (isDark) 0.60f else 0.38f
                    val scrimAlpha = (progress * (1f - dragRatio) * maxAlpha).coerceIn(0f, maxAlpha)
                    drawRect(Color.Black.copy(alpha = scrimAlpha))
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { dismissWithAnimation() }
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Floating Card Container with 16dp horizontal margin and bottom inset
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        if (coordinates.size.height > 0) {
                            sheetHeightPx = coordinates.size.height.toFloat()
                        }
                    }
                    .graphicsLayer {
                        // GPU RenderNode Phase: Direct hardware translation on GPU DisplayList (Zero-jank 120 FPS)
                        val progress = animProgress.value
                        val dragY = dragOffsetY.value
                        translationY = ((1f - progress) * sheetHeightPx) + maxOf(0f, dragY)
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity().y
                                val dragThreshold = with(density) { 70.dp.toPx() }
                                if (dragOffsetY.value > dragThreshold || velocity > 650f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dismissWithAnimation()
                                } else {
                                    scope.launch {
                                        dragOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.82f,
                                                stiffness = 420f
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    dragOffsetY.animateTo(0f, spring(0.82f, 420f))
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                val newOffset = if (dragOffsetY.value + dragAmount < 0) {
                                    dragOffsetY.value + dragAmount * 0.18f // Rubber-band elasticity
                                } else {
                                    dragOffsetY.value + dragAmount
                                }
                                scope.launch {
                                    dragOffsetY.snapTo(newOffset)
                                }
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent clicks inside the card from dismissing
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Solid Floating Modal Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(cardModifier)
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                        .then(modifier),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .size(width = 36.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(dragHandleColor)
                    )

                    // Optional Icon Badge
                    if (resolvedIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(resolvedIconTint.copy(alpha = if (isLightTheme) 0.12f else 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = resolvedIcon,
                                contentDescription = null,
                                tint = resolvedIconTint,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Modal Title
                    BasicText(
                        text = title,
                        style = TextStyle(
                            color = titleColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Modal Descriptive Message
                    BasicText(
                        text = message,
                        style = TextStyle(
                            color = subtitleColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Custom content slot if provided
                    if (content != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        content()
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons on a Single Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Secondary Action: Cancel Button (Flat Liquid Glass Surface without shadows)
                        val cancelInteractionSource = remember { MutableInteractionSource() }
                        val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()
                        val cancelScale by animateFloatAsState(
                            targetValue = if (isCancelPressed) 0.96f else 1f,
                            animationSpec = buttonSpringSpec,
                            label = "cancelButtonScale"
                        )

                        val cancelSurfaceColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                        val cancelTextColor = if (isLightTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)

                        val cancelGlassModifier = if (backdrop != null) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { buttonShape },
                                effects = {
                                    vibrancy()
                                    blur(8f.dp.toPx())
                                    lens(10f.dp.toPx(), 18f.dp.toPx())
                                },
                                highlight = { Highlight.Plain },
                                onDrawSurface = {
                                    drawRect(
                                        if (isDark) cancelSurfaceColor.copy(alpha = 0.65f)
                                        else cancelSurfaceColor.copy(alpha = 0.75f)
                                    )
                                }
                            )
                        } else {
                            Modifier
                                .clip(buttonShape)
                                .background(
                                    if (isDark) cancelSurfaceColor.copy(alpha = 0.85f)
                                    else cancelSurfaceColor.copy(alpha = 0.90f)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                                    ),
                                    buttonShape
                                )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .graphicsLayer {
                                    scaleX = cancelScale
                                    scaleY = cancelScale
                                }
                                .then(cancelGlassModifier)
                                .clickable(
                                    interactionSource = cancelInteractionSource,
                                    indication = null,
                                    role = Role.Button
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dismissWithAnimation {
                                        if (onCancel != null) {
                                            onCancel()
                                        }
                                    }
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicText(
                                text = cancelText,
                                style = TextStyle(
                                    color = cancelTextColor,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }

                        // Primary Action: Confirm / Destructive Button (Flat Liquid Glass Tinted without shadows)
                        val confirmInteractionSource = remember { MutableInteractionSource() }
                        val isConfirmPressed by confirmInteractionSource.collectIsPressedAsState()
                        val confirmScale by animateFloatAsState(
                            targetValue = if (isConfirmPressed) 0.96f else 1f,
                            animationSpec = buttonSpringSpec,
                            label = "confirmButtonScale"
                        )

                        val confirmBaseColor = if (isDestructive) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary

                        val confirmGlassModifier = if (backdrop != null) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { buttonShape },
                                effects = {
                                    vibrancy()
                                    blur(8f.dp.toPx())
                                    lens(10f.dp.toPx(), 18f.dp.toPx())
                                },
                                highlight = { Highlight.Plain },
                                onDrawSurface = {
                                    drawRect(confirmBaseColor.copy(alpha = 0.92f))
                                }
                            )
                        } else {
                            Modifier
                                .clip(buttonShape)
                                .background(confirmBaseColor)
                                .border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                    buttonShape
                                )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .graphicsLayer {
                                    scaleX = confirmScale
                                    scaleY = confirmScale
                                }
                                .then(confirmGlassModifier)
                                .clickable(
                                    interactionSource = confirmInteractionSource,
                                    indication = null,
                                    role = Role.Button
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dismissWithAnimation {
                                        onConfirm()
                                    }
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicText(
                                text = confirmText,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
