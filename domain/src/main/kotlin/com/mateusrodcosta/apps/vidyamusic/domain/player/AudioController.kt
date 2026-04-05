package com.mateusrodcosta.apps.vidyamusic.domain.player

import com.mateusrodcosta.apps.vidyamusic.domain.entity.TrackEntity
import kotlinx.coroutines.flow.StateFlow

interface AudioController {
    val playerState: StateFlow<PlayerState>
    val currentTrack: StateFlow<TrackEntity?>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val bufferedPositionMs: StateFlow<Long>
    val shuffleEnabled: StateFlow<Boolean>

    fun startPlaylist(tracks: List<TrackEntity>, startIndex: Int = 0, startPositionMs: Long = 0L)
    fun setShuffleEnabled(enabled: Boolean)

    fun currentPlaybackSnapshot(): Pair<Int, Long>?

    fun play()
    fun pause()
    fun stop()

    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
}