package com.mateusrodcosta.apps.vidyamusic.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackDto(
    val id: Int? = null,
    val game: String,
    val title: String,
    val comp: String,
    val arr: String? = null,
    val file: String,
    @SerialName("s_id") val sId: Int? = null,
    @SerialName("s_title") val sTitle: String? = null,
    @SerialName("s_file") val sFile: String? = null,
)