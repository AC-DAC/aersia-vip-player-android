package com.mateusrodcosta.apps.vidyamusic.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.preferencesDataStore
import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepositoryImpl(private val context: Context) : PreferencesRepository {
    private val THEME_KEY = stringPreferencesKey("theme_mode")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    private val SKIP_INTRO_KEY = booleanPreferencesKey("skip_intro")
    private val LAST_PLAYLIST_ID_KEY = stringPreferencesKey("last_playlist_id")
    private val BLUETOOTH_AUTOLAUNCH_KEY = booleanPreferencesKey("bluetooth_autolaunch")
    private val SHUFFLE_ENABLED_KEY = booleanPreferencesKey("shuffle_enabled")

    private fun trackIndexKey(playlistId: String) = intPreferencesKey("track_index_$playlistId")
    private fun playbackPositionKey(playlistId: String) = longPreferencesKey("playback_position_$playlistId")


    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val savedValue = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(savedValue)
    }

    override val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true
    }

    override val skipPlaylistIntro: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SKIP_INTRO_KEY] ?: false
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

    override suspend fun setUseDynamicColor(useDynamic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = useDynamic
        }
    }

    override suspend fun setSkipPlaylistIntro(skip: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SKIP_INTRO_KEY] = skip
        }
    }

    override val lastPlaylistId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_PLAYLIST_ID_KEY]
    }

    override suspend fun setLastPlaylistId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_PLAYLIST_ID_KEY] = id
        }
    }

    override suspend fun getLastTrackIndex(playlistId: String): Int =
        context.dataStore.data.map { it[trackIndexKey(playlistId)] ?: 0 }.first()

    override suspend fun setLastTrackIndex(playlistId: String, index: Int) {
        context.dataStore.edit { it[trackIndexKey(playlistId)] = index }
    }

    override suspend fun getLastPlaybackPositionMs(playlistId: String): Long =
        context.dataStore.data.map { it[playbackPositionKey(playlistId)] ?: 0L }.first()

    override suspend fun setLastPlaybackPositionMs(playlistId: String, positionMs: Long) {
        context.dataStore.edit { it[playbackPositionKey(playlistId)] = positionMs }
    }

    override val bluetoothAutoLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BLUETOOTH_AUTOLAUNCH_KEY] ?: false
    }

    override suspend fun setBluetoothAutoLaunch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BLUETOOTH_AUTOLAUNCH_KEY] = enabled
        }
    }

    override val shuffleEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHUFFLE_ENABLED_KEY] ?: false
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHUFFLE_ENABLED_KEY] = enabled
        }
    }
}