package com.mateusrodcosta.apps.vidyamusic.features.shared.components.utils

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

fun shouldShowLandscape(sizeClass: WindowSizeClass): Boolean {
    val widthSizeClass = sizeClass.widthSizeClass
    val heightSizeClass = sizeClass.heightSizeClass

    val showLandscapePhone = heightSizeClass == WindowHeightSizeClass.Compact
    val showLandscapeTablet = widthSizeClass == WindowWidthSizeClass.Expanded

    return showLandscapePhone || showLandscapeTablet
}