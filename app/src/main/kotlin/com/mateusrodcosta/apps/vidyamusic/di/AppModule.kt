package com.mateusrodcosta.apps.vidyamusic.di

import com.mateusrodcosta.apps.vidyamusic.data.player.AudioControllerImpl
import com.mateusrodcosta.apps.vidyamusic.data.player.TrackMapper
import com.mateusrodcosta.apps.vidyamusic.data.repository.ConfigRepositoryImpl
import com.mateusrodcosta.apps.vidyamusic.data.repository.PlaylistRepositoryImpl
import com.mateusrodcosta.apps.vidyamusic.data.repository.PreferencesRepositoryImpl
import com.mateusrodcosta.apps.vidyamusic.data.source.AndroidAssetFileReader
import com.mateusrodcosta.apps.vidyamusic.data.source.AssetFileReader
import com.mateusrodcosta.apps.vidyamusic.domain.player.AudioController
import com.mateusrodcosta.apps.vidyamusic.domain.repository.ConfigRepository
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PlaylistRepository
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PreferencesRepository
import com.mateusrodcosta.apps.vidyamusic.domain.usecases.GetAvailablePlaylistsUseCase
import com.mateusrodcosta.apps.vidyamusic.domain.usecases.LoadOmniPlaylistUseCase
import com.mateusrodcosta.apps.vidyamusic.domain.usecases.LoadPlaylistUseCase
import com.mateusrodcosta.apps.vidyamusic.features.playlist.PlaylistViewModel
import com.mateusrodcosta.apps.vidyamusic.features.settings.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        TrackMapper(packageName = androidContext().packageName)
    }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    single<AssetFileReader> { AndroidAssetFileReader(context = androidContext()) }
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get())
            }
        }
    }

    single<ConfigRepository> { ConfigRepositoryImpl(fileReader = get(), jsonParser = get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(client = get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl(androidContext()) }

    single<AudioController> { AudioControllerImpl(context = androidContext(), trackMapper = get()) }

    factory { LoadPlaylistUseCase(configRepository = get(), playlistRepository = get()) }
    factory { LoadOmniPlaylistUseCase(configRepository = get(), playlistRepository = get()) }
    factory { GetAvailablePlaylistsUseCase(configRepository = get()) }

    viewModel {
        PlaylistViewModel(
            loadPlaylistUseCase = get(),
            loadOmniPlaylistUseCase = get(),
            getAvailablePlaylistsUseCase = get(),
            audioController = get(),
            preferencesRepository = get()
        )
    }
    viewModel { SettingsViewModel(preferencesRepository = get(), audioController = get()) }
}