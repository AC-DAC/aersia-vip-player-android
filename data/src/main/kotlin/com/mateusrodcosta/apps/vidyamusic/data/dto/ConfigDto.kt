package com.mateusrodcosta.apps.vidyamusic.data.dto

import com.mateusrodcosta.apps.vidyamusic.core.helpers.PlaylistUrl
import com.mateusrodcosta.apps.vidyamusic.core.helpers.URLSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigFileDto(
    @SerialName("default_playlist") val defaultPlaylist: String,
    val playlists: List<PlaylistConfigDto>,
)

@Serializable
data class PlaylistConfigDto(
    val id: String,
    val order: Int,
    val name: String,
    val description: String,
    @Serializable(with = URLSerializer::class) val url: PlaylistUrl? = null,
    val format: String = "json",
    val type: String = "playlist",
    val constituents: List<String> = emptyList(),
    @SerialName("is_source") val isSource: Boolean = false,
    val extras: ExtrasDto? = null,
)

@Serializable
data class ExtrasDto(
    @SerialName("source_path") val sourcePath: String? = null,
)