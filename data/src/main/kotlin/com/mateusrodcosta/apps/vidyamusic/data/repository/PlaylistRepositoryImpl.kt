package com.mateusrodcosta.apps.vidyamusic.data.repository

import android.util.Xml
import com.mateusrodcosta.apps.vidyamusic.data.dto.PlaylistDto
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistConfigEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.PlaylistEntity
import com.mateusrodcosta.apps.vidyamusic.domain.entity.TrackEntity
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PlaylistRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class PlaylistRepositoryImpl(private val client: HttpClient) : PlaylistRepository {
    override suspend fun fetchPlaylist(playlistConfig: PlaylistConfigEntity): Result<PlaylistEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val url = playlistConfig.url
                    ?: return@withContext Result.failure(IllegalStateException("Playlist ${playlistConfig.id} has no URL"))
                val response = client.get(url)

                val playlistEntity = if (playlistConfig.format == "xml") {
                    parseXmlPlaylist(response.bodyAsText(), playlistConfig)
                } else {
                    parseJsonPlaylist(response.body(), playlistConfig)
                }

                Result.success(playlistEntity)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseJsonPlaylist(
        playlistDto: PlaylistDto,
        playlistConfig: PlaylistConfigEntity,
    ): PlaylistEntity {
        val isSourcePlaylist = playlistConfig.isSource
        val safeBaseUrl = playlistDto.url.removeSuffix("/")

        val trackEntities = playlistDto.tracks
            .filter { dto -> !isSourcePlaylist || dto.sFile != null }
            .mapIndexed { index, dto ->
                val shouldUseSource = isSourcePlaylist && dto.sFile != null

                val baseId = if (shouldUseSource) (dto.sId ?: dto.id) else dto.id
                val id = baseId ?: index

                val title = if (shouldUseSource) (dto.sTitle ?: dto.title) else dto.title
                val file = if (shouldUseSource) dto.sFile else dto.file

                val fileName = "$file.${playlistDto.ext}"

                val finalUrl =
                    if (shouldUseSource && playlistConfig.sourcePath?.isNotEmpty() == true) {
                        val safeSourcePath =
                            playlistConfig.sourcePath?.removePrefix("/")?.removeSuffix("/")
                        "$safeBaseUrl/$safeSourcePath/$fileName"
                    } else {
                        "$safeBaseUrl/$fileName"
                    }

                TrackEntity(
                    id = id,
                    game = dto.game,
                    title = title,
                    comp = dto.comp,
                    arr = if (!shouldUseSource) dto.arr else null,
                    file = file,
                    url = finalUrl
                )
            }

        return PlaylistEntity(
            id = playlistConfig.id,
            name = playlistConfig.name,
            description = playlistConfig.description,
            changelog = playlistDto.changelog,
            url = playlistDto.url,
            ext = playlistDto.ext,
            newId = playlistDto.newId,
            tracks = trackEntities,
        )
    }

    private fun parseXmlPlaylist(
        xmlContent: String,
        playlistConfig: PlaylistConfigEntity,
    ): PlaylistEntity {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlContent))

        val tracks = mutableListOf<TrackEntity>()
        var creator = ""
        var title = ""
        var location = ""
        var inTrack = false
        var currentTag = ""
        var index = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "track") {
                        inTrack = true
                        creator = ""
                        title = ""
                        location = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inTrack) {
                        when (currentTag) {
                            "creator" -> creator = parser.text.trim()
                            "title" -> title = parser.text.trim()
                            "location" -> location = parser.text.trim()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "track" && inTrack) {
                        if (location.isNotEmpty()) {
                            tracks.add(
                                TrackEntity(
                                    id = index++,
                                    game = creator,
                                    title = title,
                                    comp = "",
                                    arr = null,
                                    file = location,
                                    url = location,
                                )
                            )
                        }
                        inTrack = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        return PlaylistEntity(
            id = playlistConfig.id,
            name = playlistConfig.name,
            description = playlistConfig.description,
            tracks = tracks,
        )
    }
}
