package com.mateusrodcosta.apps.vidyamusic.domain.entity

import com.mateusrodcosta.apps.vidyamusic.core.helpers.PlaylistUrl

data class PlaylistConfigEntity(
    val id: String,
    val order: Int,
    val name: String,
    val description: String,
    val url: PlaylistUrl? = null,
    val format: String = "json",
    val isDefault: Boolean = false,
    val isComposite: Boolean = false,
    val constituentIds: List<String> = emptyList(),
    val isSource: Boolean = false,
    val sourcePath: String? = null,
)
