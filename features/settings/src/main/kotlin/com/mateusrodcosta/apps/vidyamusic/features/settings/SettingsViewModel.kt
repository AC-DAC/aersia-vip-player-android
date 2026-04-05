package com.mateusrodcosta.apps.vidyamusic.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode
import com.mateusrodcosta.apps.vidyamusic.domain.player.AudioController
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val audioController: AudioController
) : ViewModel() {
    val launchState: StateFlow<AppLaunchState?> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.useDynamicColor
    ) { theme, dynamicColor ->
        AppLaunchState(theme, dynamicColor)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentTheme: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    val useDynamicColor: StateFlow<Boolean> = preferencesRepository.useDynamicColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun updateDynamicColor(useDynamic: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUseDynamicColor(useDynamic)
        }
    }

    val skipPlaylistIntro: StateFlow<Boolean> = preferencesRepository.skipPlaylistIntro
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun updateSkipPlaylistIntro(skip: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSkipPlaylistIntro(skip)
        }
    }

    val shuffleEnabled: StateFlow<Boolean> = audioController.shuffleEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun updateShuffleEnabled(enabled: Boolean) {
        audioController.setShuffleEnabled(enabled)
        viewModelScope.launch {
            preferencesRepository.setShuffleEnabled(enabled)
        }
    }

    val bluetoothAutoLaunch: StateFlow<Boolean> = preferencesRepository.bluetoothAutoLaunch
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateBluetoothAutoLaunch(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBluetoothAutoLaunch(enabled)
        }
    }
}