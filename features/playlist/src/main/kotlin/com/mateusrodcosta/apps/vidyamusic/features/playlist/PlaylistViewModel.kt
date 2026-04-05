package com.mateusrodcosta.apps.vidyamusic.features.playlist

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistConfigEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.TrackEntity
import com.mateusrodcosta.apps.vidyamusic.domain.player.AudioController
import com.mateusrodcosta.apps.vidyamusic.domain.player.PlayerState
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PreferencesRepository
import com.mateusrodcosta.apps.vidyamusic.domain.usecases.GetAvailablePlaylistsUseCase
import com.mateusrodcosta.apps.vidyamusic.domain.usecases.LoadOmniPlaylistUseCase
import com.mateusrodcosta.apps.vidyamusic.domain.usecases.LoadPlaylistUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlaylistViewModel(
    private val loadPlaylistUseCase: LoadPlaylistUseCase,
    private val loadOmniPlaylistUseCase: LoadOmniPlaylistUseCase,
    private val getAvailablePlaylistsUseCase: GetAvailablePlaylistsUseCase,
    private val audioController: AudioController,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    private val _debugEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val debugEvents: SharedFlow<String> = _debugEvents.asSharedFlow()

    val playerState = audioController.playerState
    val currentTrack = audioController.currentTrack

    val currentPositionMs = audioController.currentPositionMs
    val bufferedPositionMs = audioController.bufferedPositionMs
    val durationMs = audioController.durationMs
    val shuffleEnabled = audioController.shuffleEnabled

    private var playingTracks: List<TrackEntity> = emptyList()

    init {
        debugLog("PlaylistViewModel init")
        fetchInitialData()
        observeTrackChangesForSave()
        observeAppBackgroundForPositionSave()
        periodicPositionSave()
    }

    private fun observeTrackChangesForSave() {
        viewModelScope.launch {
            audioController.currentTrack.collect { track ->
                if (track == null) return@collect
                val idx = playingTracks.indexOf(track)
                val playlistId = _uiState.value.selectedPlaylist?.id ?: return@collect
                debugLog("trackChange: playlist=$playlistId idx=$idx track=${track.id}")
                if (idx >= 0) {
                    preferencesRepository.setLastTrackIndex(playlistId, idx)
                    preferencesRepository.setLastPlaybackPositionMs(playlistId, 0L)
                }
            }
        }
    }

    private fun observeAppBackgroundForPositionSave() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                val snapshot = audioController.currentPlaybackSnapshot() ?: return
                val playlistId = _uiState.value.selectedPlaylist?.id ?: return
                val (index, positionMs) = snapshot
                runBlocking {
                    preferencesRepository.setLastTrackIndex(playlistId, index)
                    preferencesRepository.setLastPlaybackPositionMs(playlistId, positionMs)
                }
            }
        })
    }

    private fun periodicPositionSave() {
        viewModelScope.launch {
            while (true) {
                delay(5_000L)
                if (playerState.value == PlayerState.PLAYING) {
                    val snapshot = audioController.currentPlaybackSnapshot() ?: continue
                    val playlistId = _uiState.value.selectedPlaylist?.id ?: continue
                    val (index, positionMs) = snapshot
                    preferencesRepository.setLastTrackIndex(playlistId, index)
                    preferencesRepository.setLastPlaybackPositionMs(playlistId, positionMs)
                }
            }
        }
    }

    private fun fetchInitialData() {
        debugLog("fetchInitialData called")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            var availablePlaylists = emptyList<PlaylistConfigEntity>()
            getAvailablePlaylistsUseCase().onSuccess { playlists ->
                availablePlaylists = playlists
                _uiState.update { it.copy(availablePlaylists = playlists) }
            }

            val savedPlaylistId = preferencesRepository.lastPlaylistId.first()
            val targetConfig = availablePlaylists.find { it.id == savedPlaylistId }
                ?: availablePlaylists.firstOrNull { it.isComposite }
                ?: availablePlaylists.firstOrNull { it.isDefault }
                ?: availablePlaylists.firstOrNull()

            val result = if (targetConfig?.isComposite == true) {
                loadOmniPlaylistUseCase(targetConfig)
            } else {
                loadPlaylistUseCase(playlistId = savedPlaylistId)
            }

            result.onSuccess { playlist ->
                _uiState.update { it.copy(selectedPlaylist = playlist, isLoading = false) }

                preferencesRepository.setLastPlaylistId(playlist.id)

                if (playlist.tracks.isNotEmpty()) {
                    val skipIntro = preferencesRepository.skipPlaylistIntro.first()

                    val tracksToPlay = if (skipIntro && playlist.tracks.size > 1)
                        playlist.tracks.drop(1)
                    else
                        playlist.tracks

                    val savedIndex = preferencesRepository.getLastTrackIndex(playlist.id)
                    val startIndex = savedIndex.coerceIn(0, tracksToPlay.lastIndex)
                    val startPositionMs = preferencesRepository.getLastPlaybackPositionMs(playlist.id)
                    val shuffle = preferencesRepository.shuffleEnabled.first()

                    audioController.setShuffleEnabled(shuffle)
                    playingTracks = tracksToPlay
                    audioController.startPlaylist(tracksToPlay, startIndex, startPositionMs)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message ?: "Unknown connection error",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun debugLog(msg: String) {
        Log.d("PLAYLIST_DEBUG", msg)
    }

    fun selectPlaylist(playlistId: String) {
        debugLog("selectPlaylist called: $playlistId")
        val currentShuffle = audioController.shuffleEnabled.value
        viewModelScope.launch {
            val currentSnapshot = audioController.currentPlaybackSnapshot()
            val currentPlaylistId = _uiState.value.selectedPlaylist?.id
            debugLog("outgoing: playlist=$currentPlaylistId snapshot=$currentSnapshot")
            if (currentSnapshot != null && currentPlaylistId != null) {
                val (index, positionMs) = currentSnapshot
                debugLog("saving outgoing: playlist=$currentPlaylistId index=$index")
                preferencesRepository.setLastTrackIndex(currentPlaylistId, index)
                preferencesRepository.setLastPlaybackPositionMs(currentPlaylistId, positionMs)
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val config = _uiState.value.availablePlaylists.find { it.id == playlistId }

            val result = if (config?.isComposite == true) {
                loadOmniPlaylistUseCase(config)
            } else {
                loadPlaylistUseCase(playlistId)
            }

            result.onSuccess { playlist ->
                _uiState.update { it.copy(selectedPlaylist = playlist, isLoading = false) }

                preferencesRepository.setLastPlaylistId(playlist.id)

                if (playlist.tracks.isNotEmpty()) {
                    val skipIntro = preferencesRepository.skipPlaylistIntro.first()

                    val tracksToPlay = if (skipIntro && playlist.tracks.size > 1)
                        playlist.tracks.drop(1)
                    else
                        playlist.tracks

                    val savedIndex = preferencesRepository.getLastTrackIndex(playlist.id)
                    val startIndex = savedIndex.coerceIn(0, tracksToPlay.lastIndex)

                    audioController.setShuffleEnabled(currentShuffle)

                    debugLog("playlist: ${playlist.id} | savedIndex: $savedIndex | startIndex: $startIndex | shuffle: $currentShuffle")

                    playingTracks = tracksToPlay
                    audioController.startPlaylist(tracksToPlay, startIndex, 0L)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message ?: "Unknown connection error",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun reload() {
        val currentPlaylistId = _uiState.value.selectedPlaylist?.id

        if (currentPlaylistId != null) {
            selectPlaylist(currentPlaylistId)
        } else {
            fetchInitialData()
        }
    }

    fun playTrack(trackIndex: Int) {
        val currentPlaylist = _uiState.value.selectedPlaylist?.tracks ?: return
        playingTracks = currentPlaylist
        audioController.startPlaylist(currentPlaylist, trackIndex, 0L)
    }

    fun togglePlayPause() {
        if (playerState.value == PlayerState.PLAYING) {
            audioController.pause()
        } else {
            audioController.play()
        }
    }

    fun skipToNext() {
        audioController.skipToNext()
    }

    fun skipToPrevious() {
        audioController.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        audioController.seekTo(positionMs)
    }

    fun toggleShuffle() {
        val newValue = !shuffleEnabled.value
        audioController.setShuffleEnabled(newValue)
        viewModelScope.launch {
            preferencesRepository.setShuffleEnabled(newValue)
        }
    }
}
