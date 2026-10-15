package com.buwin.magiccapsule.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.buwin.magiccapsule.ui.components.liquid.LiquidBottomTab
import com.buwin.magiccapsule.ui.components.liquid.LiquidBottomTabs
import com.buwin.magiccapsule.ui.screens.FeaturesOverviewScreen
import com.buwin.magiccapsule.ui.screens.HomeScreen
import com.buwin.magiccapsule.ui.screens.SettingsConfigScreen
import com.buwin.magiccapsule.ui.screens.SubpageType
import com.buwin.magiccapsule.ui.screens.subpages.ApiIslandSubPage
import com.buwin.magiccapsule.ui.screens.subpages.CalendarSubPage
import com.buwin.magiccapsule.ui.screens.subpages.CustomCardSubPage
import com.buwin.magiccapsule.ui.screens.subpages.DeviceAlertSubPage
import com.buwin.magiccapsule.ui.screens.subpages.DeviceIslandSubPage
import com.buwin.magiccapsule.ui.screens.subpages.FaceUnlockSubPage
import com.buwin.magiccapsule.ui.screens.subpages.HeadsetSettingsSubPage
import com.buwin.magiccapsule.ui.screens.subpages.HeartRateSubPage
import com.buwin.magiccapsule.ui.screens.subpages.IslandBehaviorSubPage
import com.buwin.magiccapsule.ui.screens.subpages.LyricsSettingsSubPage
import com.buwin.magiccapsule.ui.screens.subpages.NotificationIslandSubPage
import com.buwin.magiccapsule.ui.screens.subpages.RefreshRateSubPage
import com.buwin.magiccapsule.ui.screens.subpages.ScrollSettingsSubPage
import com.buwin.magiccapsule.ui.screens.subpages.SmsSettingsSubPage
import com.buwin.magiccapsule.ui.screens.subpages.ThemeSettingsSubPage
import com.buwin.magiccapsule.ui.theme.BackgroundDark
import com.buwin.magiccapsule.ui.theme.BackgroundLight
import com.buwin.magiccapsule.ui.theme.LocalIsDark
import com.buwin.magiccapsule.ui.theme.ThemePreferences
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.Gearshape
import io.github.alexzhirkevich.cupertino.icons.outlined.SliderHorizontal3
import io.github.alexzhirkevich.cupertino.icons.outlined.Sparkles
import kotlinx.coroutines.delay

/**
 * Root tab and subpage navigation container with authentic Liquid Glass navigation bar.
 * Matches Tier 1 Architecture from NoticeFlow reference app:
 * - Pre-warmed alive tabs for buttery-smooth 120 FPS tab switching with zero lag
 * - Tab 0: "Chức năng" (Overview & quick controls)
 * - Tab 1: "Cấu hình" (Detailed configuration of every single feature matching NoticeFlow)
 * - Tab 2: "Giao diện" (Liquid Glass optics, theme mode, colors)
 */
@Composable
fun MainTabContainer(
    themePreferences: ThemePreferences,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activeSubpage by remember { mutableStateOf<SubpageType?>(null) }

    // Intercept back key to pop subpages
    BackHandler(enabled = activeSubpage != null) {
        activeSubpage = null
    }

    val screenBackdrop = rememberLayerBackdrop()
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight

    // Keep tabs alive in memory once created, pre-warming sequentially during idle time
    val loadedTabs = remember { mutableStateListOf(0) }

    LaunchedEffect(Unit) {
        delay(400)
        if (!loadedTabs.contains(1)) loadedTabs.add(1)
        delay(200)
        if (!loadedTabs.contains(2)) loadedTabs.add(2)
    }

    LaunchedEffect(selectedTabIndex) {
        if (!loadedTabs.contains(selectedTabIndex)) {
            loadedTabs.add(selectedTabIndex)
        }
    }

    val isBottomBarVisible = activeSubpage == null
    val contentColor = if (isDark) Color.White else Color.Black

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .layerBackdrop(screenBackdrop)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            if (activeSubpage != null) {
                when (activeSubpage!!) {
                    SubpageType.POWER -> HomeScreen(
                        themePreferences = themePreferences,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestNotificationPermission = onRequestNotificationPermission,
                        onBack = { activeSubpage = null }
                    )
                    SubpageType.DEVICE -> DeviceIslandSubPage(onBack = { activeSubpage = null })
                    SubpageType.SMS -> SmsSettingsSubPage(onBack = { activeSubpage = null })
                    SubpageType.LYRICS -> LyricsSettingsSubPage(onBack = { activeSubpage = null })
                    SubpageType.NOTIFY -> NotificationIslandSubPage(onBack = { activeSubpage = null })
                    SubpageType.HEADSET -> HeadsetSettingsSubPage(onBack = { activeSubpage = null })
                    SubpageType.FACE_UNLOCK -> FaceUnlockSubPage(onBack = { activeSubpage = null })
                    SubpageType.CALENDAR -> CalendarSubPage(onBack = { activeSubpage = null })
                    SubpageType.API -> ApiIslandSubPage(onBack = { activeSubpage = null })
                    SubpageType.THEME -> ThemeSettingsSubPage(
                        themePreferences = themePreferences,
                        onBack = { activeSubpage = null }
                    )
                    SubpageType.ALERT -> DeviceAlertSubPage(onBack = { activeSubpage = null })
                    SubpageType.CUSTOM_CARD -> CustomCardSubPage(onBack = { activeSubpage = null })
                    SubpageType.REFRESH_RATE -> RefreshRateSubPage(onBack = { activeSubpage = null })
                    SubpageType.ISLAND_BEHAVIOR -> IslandBehaviorSubPage(onBack = { activeSubpage = null })
                    SubpageType.SCROLL -> ScrollSettingsSubPage(onBack = { activeSubpage = null })
                    SubpageType.HEART_RATE -> HeartRateSubPage(onBack = { activeSubpage = null })
                }
            } else {
                // ── Pre-warmed alive tabs for buttery-smooth 120 FPS tab switching ──
                if (loadedTabs.contains(0)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .keepAliveTab(selectedTabIndex == 0)
                    ) {
                        FeaturesOverviewScreen(
                            themePreferences = themePreferences,
                            onNavigateToSubpage = { subpage -> activeSubpage = subpage }
                        )
                    }
                }

                if (loadedTabs.contains(1)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .keepAliveTab(selectedTabIndex == 1)
                    ) {
                        SettingsConfigScreen(
                            themePreferences = themePreferences,
                            onNavigateToSubpage = { subpage -> activeSubpage = subpage }
                        )
                    }
                }

                if (loadedTabs.contains(2)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .keepAliveTab(selectedTabIndex == 2)
                    ) {
                        ThemeSettingsSubPage(
                            themePreferences = themePreferences,
                            onBack = { selectedTabIndex = 0 }
                        )
                    }
                }
            }
        }

        // Floating Liquid Glass Bottom Tabs
        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        ) {
            LiquidBottomTabs(
                selectedTabIndex = { selectedTabIndex },
                onTabSelected = { selectedTabIndex = it },
                backdrop = screenBackdrop,
                tabsCount = 3,
                isDark = isDark
            ) {
                // Tab 0: Features Overview ("Chức năng")
                LiquidBottomTab(onClick = { selectedTabIndex = 0 }) {
                    Icon(
                        imageVector = CupertinoIcons.Outlined.Sparkles,
                        contentDescription = "Chức năng",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    BasicText(
                        text = "Chức năng",
                        style = TextStyle(
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                // Tab 1: Detailed Configuration & Customization ("Tùy chỉnh")
                LiquidBottomTab(onClick = { selectedTabIndex = 1 }) {
                    Icon(
                        imageVector = CupertinoIcons.Outlined.SliderHorizontal3,
                        contentDescription = "Tùy chỉnh",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    BasicText(
                        text = "Tùy chỉnh",
                        style = TextStyle(
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                // Tab 2: Settings & Appearance ("Cài đặt")
                LiquidBottomTab(onClick = { selectedTabIndex = 2 }) {
                    Icon(
                        imageVector = CupertinoIcons.Filled.Gearshape,
                        contentDescription = "Cài đặt",
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    BasicText(
                        text = "Cài đặt",
                        style = TextStyle(
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Keeps inactive tabs alive in composition without drawing or intercepting touch events.
 */
private fun Modifier.keepAliveTab(visible: Boolean): Modifier = this
    .graphicsLayer {
        alpha = if (visible) 1f else 0f
    }
    .zIndex(if (visible) 1f else 0f)
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            if (visible) {
                placeable.place(0, 0)
            } else {
                placeable.place(-100000, -100000)
            }
        }
    }
