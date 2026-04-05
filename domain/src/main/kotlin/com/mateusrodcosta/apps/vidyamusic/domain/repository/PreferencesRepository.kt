package com.mateusrodcosta.apps.vidyamusic.domain.repository

import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val useDynamicColor: Flow<Boolean>
    val skipPlaylistIntro: Flow<Boolean>
    val lastPlaylistId: Flow<String?>
    val bluetoothAutoLaunch: Flow<Boolean>
    val shuffleEnabled: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setUseDynamicColor(useDynamic: Boolean)
    suspend fun setSkipPlaylistIntro(skip: Boolean)
    suspend fun setLastPlaylistId(id: String)
    suspend fun getLastTrackIndex(playlistId: String): Int
    suspend fun setLastTrackIndex(playlistId: String, index: Int)
    suspend fun getLastPlaybackPositionMs(playlistId: String): Long
    suspend fun setLastPlaybackPositionMs(playlistId: String, positionMs: Long)
    suspend fun setBluetoothAutoLaunch(enabled: Boolean)
    suspend fun setShuffleEnabled(enabled: Boolean)
}