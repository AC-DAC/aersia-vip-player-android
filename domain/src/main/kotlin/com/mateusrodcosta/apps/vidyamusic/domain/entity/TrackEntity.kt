package com.mateusrodcosta.apps.vidyamusic.domain.entity

data class TrackEntity(
    val id: Int? = null,
    val game: String,
    val title: String,
    val comp: String,
    val arr: String? = null,
    val file: String,
    val url: String,
)
