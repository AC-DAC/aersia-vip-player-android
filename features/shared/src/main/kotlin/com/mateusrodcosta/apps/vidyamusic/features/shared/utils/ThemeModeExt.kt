package com.mateusrodcosta.apps.vidyamusic.features.shared.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode
import com.mateusrodcosta.apps.vidyamusic.features.shared.R
import com.mateusrodcosta.apps.vidyamusic.features.shared.models.ThemeUiInfo

val ThemeMode.uiInfo: ThemeUiInfo
    @Composable
    get() = when (this) {
        ThemeMode.SYSTEM -> ThemeUiInfo(
            text = stringResource(R.string.theme_mode_system),
            icon = Icons.Default.BrightnessAuto,
            label = stringResource(R.string.theme_mode_system_short)
        )

        ThemeMode.LIGHT -> ThemeUiInfo(
            text = stringResource(R.string.theme_mode_light),
            icon = Icons.Default.LightMode,
            label = stringResource(R.string.theme_mode_light_short)
        )

        ThemeMode.DARK -> ThemeUiInfo(
            text = stringResource(R.string.theme_mode_dark),
            icon = Icons.Default.DarkMode,
            label = stringResource(R.string.theme_mode_dark_short)
        )
    }