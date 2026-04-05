package com.mateusrodcosta.apps.vidyamusic.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDto(
    val changelog: String,
    val url: String,
    val ext: String,
    @SerialName("new_id") val newId: Int? = null,
    val tracks: List<TrackDto>
)
