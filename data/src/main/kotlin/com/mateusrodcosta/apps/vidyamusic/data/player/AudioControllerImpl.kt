package com.mateusrodcosta.apps.vidyamusic.data.player

import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.mateusrodcosta.apps.vidyamusic.data.R
import com.mateusrodcosta.apps.vidyamusic.domain.entity.TrackEntity
import com.mateusrodcosta.apps.vidyamusic.domain.player.AudioController
import com.mateusrodcosta.apps.vidyamusic.domain.player.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioControllerImpl(
    private val context: Context,
    private val trackMapper: TrackMapper
) : AudioController {

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    override val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    override val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _bufferedPositionMs = MutableStateFlow(0L)
    override val bufferedPositionMs: StateFlow<Long> = _bufferedPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var currentPlaylist: List<TrackEntity> = emptyList()
    private var pendingShuffleEnabled: Boolean? = null

    init {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
            pendingShuffleEnabled?.let { mediaController?.shuffleModeEnabled = it }
            pendingShuffleEnabled = null
        }, ContextCompat.getMainExecutor(context))
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) = updateState()
        override fun onIsPlayingChanged(isPlaying: Boolean) = updateState()
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleEnabled.value = shuffleModeEnabled
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val trackId = mediaItem?.mediaId?.toIntOrNull()
            _currentTrack.value = currentPlaylist.find { it.id == trackId }
        }

        override fun onPlayerError(error: PlaybackException) {
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED
            ) {

                Toast.makeText(
                    context,
                    R.string.audio_controller_lost_connection,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateState() {
        val player = mediaController ?: return

        _playerState.value = when {
            player.playbackState == Player.STATE_BUFFERING -> PlayerState.BUFFERING
            player.isPlaying -> PlayerState.PLAYING
            player.playbackState == Player.STATE_READY && !player.isPlaying -> PlayerState.PAUSED
            player.playbackState == Player.STATE_IDLE -> PlayerState.IDLE
            else -> PlayerState.ERROR
        }

        _durationMs.value = player.duration.coerceAtLeast(0L)
        _bufferedPositionMs.value = player.bufferedPosition.coerceAtLeast(0L)
        _currentPositionMs.value = player.currentPosition.coerceAtLeast(0L)

        if (player.isPlaying) {
            startPollingProgress()
        } else {
            stopPollingProgress()
        }
    }

    private fun startPollingProgress() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (true) {
                _currentPositionMs.value = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
                _bufferedPositionMs.value =
                    mediaController?.bufferedPosition?.coerceAtLeast(0L) ?: 0L
                delay(1000L)
            }
        }
    }

    private fun stopPollingProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun startPlaylist(tracks: List<TrackEntity>, startIndex: Int, startPositionMs: Long) {
        currentPlaylist = tracks

        val mediaItems = tracks.map { trackMapper.mapToMediaItem(it) }

        mediaController?.apply {
            val shuffle = _shuffleEnabled.value
            val effectiveIndex = if (shuffle) C.INDEX_UNSET else startIndex
            val effectivePosition = if (shuffle) C.TIME_UNSET else startPositionMs
            shuffleModeEnabled = shuffle
            setMediaItems(mediaItems, effectiveIndex, effectivePosition)
            prepare()
            play()
        }
    }

    override fun play() {
        if (mediaController?.playbackState == Player.STATE_IDLE) {
            mediaController?.prepare()
        }
        mediaController?.play()
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun stop() {
        mediaController?.stop()
    }

    override fun skipToNext() {
        mediaController?.seekToNext()
    }

    override fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    override fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    override fun currentPlaybackSnapshot(): Pair<Int, Long>? {
        val controller = mediaController ?: return null
        val index = controller.currentMediaItemIndex
        val position = controller.currentPosition.coerceAtLeast(0L)
        return Pair(index, position)
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        _shuffleEnabled.value = enabled
        if (mediaController != null) {
            mediaController?.shuffleModeEnabled = enabled
        } else {
            pendingShuffleEnabled = enabled
        }
    }
}