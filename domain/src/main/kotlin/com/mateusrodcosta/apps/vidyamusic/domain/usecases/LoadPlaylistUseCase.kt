package com.mateusrodcosta.apps.vidyamusic.domain.usecases

import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistEntity
import com.mateusrodcosta.apps.vidyamusic.domain.repository.ConfigRepository
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PlaylistRepository

class LoadPlaylistUseCase(
    private val configRepository: ConfigRepository,
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(playlistId: String? = null): Result<PlaylistEntity> {
        val configResult = configRepository.loadConfig()

        return configResult.fold(
            onSuccess = { config ->
                val defaultPlaylist =
                    if (playlistId != null) {
                        config.playlists.find { it.id == playlistId }
                            ?: return Result.failure(IllegalArgumentException("Playlist $playlistId not found"))
                    } else {
                        config.playlists.firstOrNull { it.isDefault } ?: config.playlists.first()
                    }
                playlistRepository.fetchPlaylist(defaultPlaylist)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}