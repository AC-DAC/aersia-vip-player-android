package com.mateusrodcosta.apps.vidyamusic.features.playlist.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mateusrodcosta.apps.vidyamusic.domain.player.PlayerState
import com.mateusrodcosta.apps.vidyamusic.features.playlist.PlaylistViewModel
import com.mateusrodcosta.apps.vidyamusic.features.playlist.components.FullScreenError
import com.mateusrodcosta.apps.vidyamusic.features.playlist.components.FullScreenPlayer
import com.mateusrodcosta.apps.vidyamusic.features.playlist.components.MiniPlayer
import com.mateusrodcosta.apps.vidyamusic.features.playlist.components.PlaylistSelector
import com.mateusrodcosta.apps.vidyamusic.features.playlist.components.PlaylistSelectorItem
import com.mateusrodcosta.apps.vidyamusic.features.playlist.components.TrackItem
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import com.mateusrodcosta.apps.vidyamusic.features.playlist.R as RPlaylist
import com.mateusrodcosta.apps.vidyamusic.features.shared.R as RShared

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel,
    onSettingsClick: () -> Unit,
    expandPlayer: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val localContext = LocalContext.current

    val state by viewModel.uiState.collectAsState()
    var showPlaylistSelector by remember { mutableStateOf(false) }

    val currentTrack by viewModel.currentTrack.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val bufferedPositionMs by viewModel.bufferedPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val playlistSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val peekHeight = if (currentTrack != null) 80.dp + navBarBottomPadding else 0.dp

    val listState = rememberLazyListState()
    val tracks = state.selectedPlaylist?.tracks ?: emptyList()

    val indicatorLabels = remember(tracks) {
        if (tracks.isEmpty()) return@remember emptyList<String>()

        val labels = ArrayList<String>(tracks.size)
        val seenFirst = mutableSetOf<Char>()
        val seenSecond = mutableSetOf<String>()

        fun normalize(name: String): String {
            var clean = name.trim()
            val lower = clean.lowercase()

            // Improve "The Legend of Zelda" handling plus similar cases
            if (lower.startsWith("the ")) clean = clean.substring(4)
            else if (lower.startsWith("an ")) clean = clean.substring(3)
            else if (lower.startsWith("a ")) clean = clean.substring(2)

            // Remove special characters
            clean = clean.filter { it.isLetterOrDigit() }

            return clean.ifEmpty { name.trim() }
        }

        for (i in tracks.indices) {
            if (i == 0) {
                labels.add("♪")
                val normUpper = normalize(tracks[i].game).uppercase()
                if (normUpper.isNotEmpty()) {
                    seenFirst.add(normUpper[0])
                    if (normUpper.length >= 2) seenSecond.add(normUpper.substring(0, 2))
                }
                continue
            }

            val rawGame = tracks[i].game
            val normUpper = normalize(rawGame).uppercase()

            if (normUpper.isEmpty()) {
                labels.add("#")
                continue
            }

            val c1 = normUpper[0]
            val prefix2 = if (normUpper.length >= 2) normUpper.substring(0, 2) else normUpper

            val labelText = if (!seenFirst.contains(c1)) {
                normUpper.substring(0, 1)
            } else if (normUpper.length >= 2 && !seenSecond.contains(prefix2)) {
                normUpper.substring(0, 2)
            } else {
                normUpper.substring(0, minOf(3, normUpper.length))
            }

            val formattedLabel = labelText.lowercase().replaceFirstChar { it.uppercase() }
            labels.add(formattedLabel)

            seenFirst.add(c1)
            if (normUpper.length >= 2) seenSecond.add(prefix2)
        }
        labels
    }

    BottomSheetScaffold(
        sheetContent = {
            val isExpanded =
                bottomSheetScaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                if (currentTrack != null) {
                    if (isExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            FullScreenPlayer(
                                track = currentTrack!!,
                                playlistName = state.selectedPlaylist?.name ?: "Unknown",
                                state = playerState,
                                currentPositionMs = currentPositionMs,
                                bufferedPositionMs = bufferedPositionMs,
                                durationMs = durationMs,
                                onSeek = { position -> viewModel.seekTo(position) },
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onNextClick = { viewModel.skipToNext() },
                                onPreviousClick = { viewModel.skipToPrevious() },
                                onCollapseClick = {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                    }
                                }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(peekHeight)
                                .padding(bottom = navBarBottomPadding)
                        ) {
                            MiniPlayer(
                                track = currentTrack!!,
                                isPlaying = playerState == PlayerState.PLAYING,
                                currentPositionMs = currentPositionMs,
                                bufferedPositionMs = bufferedPositionMs,
                                durationMs = durationMs,
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onNextClick = { viewModel.skipToNext() },
                                onSeek = { position -> viewModel.seekTo(position) }
                            )
                        }
                    }
                }
            }
        },
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = peekHeight,
        sheetDragHandle = null,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        topBar = {
            TopAppBar(
                title =
                    {
                        PlaylistSelector(
                            state.selectedPlaylist,
                            onClick = { showPlaylistSelector = true }
                        )
                    },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(RShared.string.description_icon_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = peekHeight
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    FullScreenError(
                        errorMessage = stringResource(RPlaylist.string.playlist_no_connection_error),
                        onRetry = { viewModel.reload() }
                    )
                } else if (state.selectedPlaylist != null) {
                    LazyColumnScrollbar(
                        state = listState,
                        settings = ScrollbarSettings.Default.copy(
                            thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            thumbSelectedColor = MaterialTheme.colorScheme.primary,
                            alwaysShowScrollbar = true
                        ),
                        indicatorContent = { index, isThumbSelected ->
                            if (isThumbSelected) {
                                val label = indicatorLabels.getOrNull(index) ?: "#"

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                top = 0.dp,
                                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                                bottom = 16.dp
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(tracks) { index, track ->
                                val isThisTrackPlaying =
                                    currentTrack?.id == track.id && playerState == PlayerState.PLAYING

                                TrackItem(
                                    track = track,
                                    isPlaying = isThisTrackPlaying,
                                    onClick = {
                                        if (currentTrack?.id == track.id) {
                                            viewModel.togglePlayPause()
                                        } else {
                                            viewModel.playTrack(index)
                                        }
                                    },
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                } else {
                    FullScreenError(
                        errorMessage = stringResource(RPlaylist.string.playlist_no_connection_error),
                        onRetry = { viewModel.reload() }
                    )
                }
            }
        }
    }

    if (showPlaylistSelector) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistSelector = false },
            sheetState = playlistSheetState,
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0) }
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                ) {


                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(RPlaylist.string.playlist_screen_playist_selector_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        items(state.availablePlaylists) { config ->
                            val isSelected = config.id == state.selectedPlaylist?.id

                            PlaylistSelectorItem(
                                config = config,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.selectPlaylist(config.id)
                                    showPlaylistSelector = false
                                })
                        }

                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    LaunchedEffect(expandPlayer) {
        if (expandPlayer) {
            bottomSheetScaffoldState.bottomSheetState.expand()
        }
    }

    LaunchedEffect(currentTrack) {
        if (currentTrack != null && tracks.isNotEmpty()) {
            val index = tracks.indexOfFirst { it.id == currentTrack?.id }

            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.debugEvents.collect { message ->
            Toast.makeText(localContext, message, Toast.LENGTH_LONG).show()
        }
    }
}