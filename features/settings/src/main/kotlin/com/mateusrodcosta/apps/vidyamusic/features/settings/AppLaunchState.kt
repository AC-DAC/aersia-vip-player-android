package com.mateusrodcosta.apps.vidyamusic.features.settings

import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode

data class AppLaunchState(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean
)
