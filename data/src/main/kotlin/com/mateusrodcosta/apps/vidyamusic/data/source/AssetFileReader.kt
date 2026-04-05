package com.mateusrodcosta.apps.vidyamusic.data.source

interface AssetFileReader {
    fun readJsonFile(fileName: String): String
}