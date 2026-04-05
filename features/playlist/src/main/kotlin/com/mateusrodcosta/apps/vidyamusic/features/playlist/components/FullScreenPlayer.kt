package com.mateusrodcosta.apps.vidyamusic.features.playlist.components

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mateusrodcosta.apps.vidyamusic.domain.entity.TrackEntity
import com.mateusrodcosta.apps.vidyamusic.domain.player.PlayerState
import com.mateusrodcosta.apps.vidyamusic.features.shared.components.utils.shouldShowLandscape

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun FullScreenPlayer(
    track: TrackEntity,
    playlistName: String,
    state: PlayerState,
    currentPositionMs: Long,
    bufferedPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = calculateWindowSizeClass(activity = LocalActivity.current!!)
    val useLandscapeLayout = shouldShowLandscape(windowSizeClass)

    val isPlaying = state == PlayerState.PLAYING

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FullScreenPlayerHeader(
            playlistName = playlistName,
            onCollapseClick = onCollapseClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (useLandscapeLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 48.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FullScreenPlayerCoverArt(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(start = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    FullScreenPlayerTrackInfo(track)

                    Spacer(modifier = Modifier.height(32.dp))

                    FullScreenPlayerSeekBar(
                        currentPositionMs = currentPositionMs,
                        bufferedPositionMs = bufferedPositionMs,
                        durationMs = durationMs,
                        onSeek = onSeek
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FullScreenPlayerControls(
                        isPlaying = isPlaying,
                        onPlayPauseClick = onPlayPauseClick,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick
                    )

                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FullScreenPlayerCoverArt()

                Spacer(modifier = Modifier.height(24.dp))

                FullScreenPlayerTrackInfo(track)

                Spacer(modifier = Modifier.weight(1f))

                FullScreenPlayerSeekBar(
                    currentPositionMs = currentPositionMs,
                    bufferedPositionMs = bufferedPositionMs,
                    durationMs = durationMs,
                    onSeek = onSeek
                )

                Spacer(modifier = Modifier.height(24.dp))

                FullScreenPlayerControls(
                    isPlaying = isPlaying,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}