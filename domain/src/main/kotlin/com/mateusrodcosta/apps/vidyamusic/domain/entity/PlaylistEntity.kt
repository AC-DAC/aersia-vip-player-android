package com.mateusrodcosta.apps.vidyamusic.domain.entity

data class PlaylistEntity(
    val id: String,
    val name: String,
    val description: String,
    val changelog: String = "",
    val url: String = "",
    val ext: String = "",
    val newId: Int? = null,
    val tracks: List<TrackEntity>
)
