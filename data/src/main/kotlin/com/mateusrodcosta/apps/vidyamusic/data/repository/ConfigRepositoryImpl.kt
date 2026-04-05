package com.mateusrodcosta.apps.vidyamusic.data.repository

import com.mateusrodcosta.apps.vidyamusic.data.dto.ConfigFileDto
import com.mateusrodcosta.apps.vidyamusic.data.source.AssetFileReader
import com.mateusrodcosta.apps.vidyamusic.domain.entity.ConfigFileEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistConfigEntity
import com.mateusrodcosta.apps.vidyamusic.domain.repository.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ConfigRepositoryImpl(
    private val fileReader: AssetFileReader,
    private val jsonParser: Json,
    private val configFileName: String = "config.json",
) : ConfigRepository {
    private var currentConfig: ConfigFileEntity? = null

    override suspend fun loadConfig(): Result<ConfigFileEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = fileReader.readJsonFile(configFileName)
                val configFileDto = jsonParser.decodeFromString<ConfigFileDto>(jsonString)

                val playlistConfigEntities = configFileDto.playlists.map { dto ->
                    PlaylistConfigEntity(
                        id = dto.id,
                        order = dto.order,
                        name = dto.name,
                        description = dto.description,
                        url = dto.url,
                        format = dto.format,
                        isDefault = (dto.id == configFileDto.defaultPlaylist),
                        isComposite = dto.type == "omni",
                        constituentIds = dto.constituents,
                        isSource = dto.isSource,
                        sourcePath = dto.extras?.sourcePath
                    )
                }
                val configFileEntity = ConfigFileEntity(
                    defaultPlaylist = configFileDto.defaultPlaylist,
                    playlists = playlistConfigEntities
                )

                currentConfig = configFileEntity
                Result.success(configFileEntity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAvailablePlaylists(): Result<List<PlaylistConfigEntity>> {
        val cachedConfig = currentConfig
        if (cachedConfig != null) {
            return Result.success(cachedConfig.playlists)
        }

        val newConfig = loadConfig()
        return newConfig.map { config -> config.playlists }
    }
}
