package com.mateusrodcosta.apps.vidyamusic.features.playlist

import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistConfigEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistEntity

data class PlaylistUiState(
    val isLoading: Boolean = true,
    val selectedPlaylist: PlaylistEntity? = null,
    val availablePlaylists: List<PlaylistConfigEntity> = emptyList(),
    val error: String? = null
)