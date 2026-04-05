package com.mateusrodcosta.apps.vidyamusic.domain.usecases

import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistConfigEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistEntity
import com.mateusrodcosta.apps.vidyamusic.domain.repository.ConfigRepository
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.Collator
import java.util.Locale

class LoadOmniPlaylistUseCase(
    private val configRepository: ConfigRepository,
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(playlistConfig: PlaylistConfigEntity): Result<PlaylistEntity> {
        val configResult = configRepository.loadConfig()
        val config = configResult.getOrElse { return Result.failure(it) }

        val constituentConfigs = playlistConfig.constituentIds.mapNotNull { id ->
            config.playlists.find { it.id == id }
        }

        if (constituentConfigs.isEmpty()) {
            return Result.failure(IllegalArgumentException("Omni playlist has no resolvable constituents"))
        }

        return try {
            coroutineScope {
                val deferreds = constituentConfigs.map { constituent ->
                    async { playlistRepository.fetchPlaylist(constituent) }
                }

                val results = deferreds.awaitAll()
                val failure = results.firstOrNull { it.isFailure }
                if (failure != null) {
                    return@coroutineScope Result.failure(failure.exceptionOrNull()!!)
                }

                val collator = Collator.getInstance(Locale.getDefault()).apply {
                    strength = Collator.PRIMARY
                }
                val mergedTracks = results
                    .flatMap { it.getOrThrow().tracks }
                    .sortedWith(Comparator { a, b ->
                        collator.compare(a.game + " " + a.title, b.game + " " + b.title)
                    })
                    .mapIndexed { index, track -> track.copy(id = index) }

                Result.success(
                    PlaylistEntity(
                        id = playlistConfig.id,
                        name = playlistConfig.name,
                        description = playlistConfig.description,
                        tracks = mergedTracks,
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
