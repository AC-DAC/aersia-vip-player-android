package com.mateusrodcosta.apps.vidyamusic.domain.entity

data class ConfigFileEntity(
    val defaultPlaylist: String,
    val playlists: List<PlaylistConfigEntity>,
)