package com.mateusrodcosta.apps.vidyamusic.domain.player

enum class PlayerState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}