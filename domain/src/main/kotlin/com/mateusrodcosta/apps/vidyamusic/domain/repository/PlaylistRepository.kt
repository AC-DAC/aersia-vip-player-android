package com.mateusrodcosta.apps.vidyamusic.domain.repository

import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistConfigEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistEntity

interface PlaylistRepository {
    suspend fun fetchPlaylist(playlistConfig: PlaylistConfigEntity): Result<PlaylistEntity>
}