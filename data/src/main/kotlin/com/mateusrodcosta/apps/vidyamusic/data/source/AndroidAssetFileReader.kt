package com.mateusrodcosta.apps.vidyamusic.data.source

import android.content.Context
import java.io.InputStreamReader

class AndroidAssetFileReader(private val context: Context) : AssetFileReader {
    override fun readJsonFile(fileName: String): String {
        return context.assets.open(fileName).use { inputStream ->
            InputStreamReader(inputStream).readText()
        }
    }
}