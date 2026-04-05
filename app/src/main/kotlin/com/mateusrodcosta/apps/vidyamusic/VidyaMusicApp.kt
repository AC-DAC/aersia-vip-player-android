package com.mateusrodcosta.apps.vidyamusic

import android.app.Application
import com.mateusrodcosta.apps.vidyamusic.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VidyaMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@VidyaMusicApp)
            modules(appModule)
        }
    }
}