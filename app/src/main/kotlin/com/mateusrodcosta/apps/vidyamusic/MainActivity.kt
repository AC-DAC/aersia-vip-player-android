package com.mateusrodcosta.apps.vidyamusic

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mateusrodcosta.apps.vidyamusic.core.enums.ThemeMode
import com.mateusrodcosta.apps.vidyamusic.features.playlist.PlaylistViewModel
import com.mateusrodcosta.apps.vidyamusic.features.playlist.screens.PlaylistScreen
import com.mateusrodcosta.apps.vidyamusic.features.settings.SettingsDestination
import com.mateusrodcosta.apps.vidyamusic.features.settings.SettingsViewModel
import com.mateusrodcosta.apps.vidyamusic.features.settings.screens.AboutScreen
import com.mateusrodcosta.apps.vidyamusic.features.settings.screens.SettingsScreen
import com.mateusrodcosta.apps.vidyamusic.features.shared.components.NotificationPermissionHandler
import com.mateusrodcosta.apps.vidyamusic.features.shared.ui.theme.VidyaMusicTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.launchState.value == null
        }

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            )

            fadeOut.duration = 400L
            fadeOut.interpolator = AnticipateInterpolator()

            fadeOut.doOnEnd {
                splashScreenViewProvider.remove()
            }

            fadeOut.start()
        }

        setContent {
            val state by settingsViewModel.launchState.collectAsStateWithLifecycle()

            state?.let { launchState ->
                val isDarkTheme = when (launchState.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                val barStyle = if (isDarkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }

                LaunchedEffect(isDarkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = barStyle,
                        navigationBarStyle = barStyle
                    )
                }

                VidyaMusicTheme(
                    darkTheme = isDarkTheme,
                    dynamicColor = launchState.useDynamicColor,
                ) {
                    val viewModel: PlaylistViewModel = koinViewModel()
                    val snackbarHostState = remember { SnackbarHostState() }

                    NotificationPermissionHandler(snackbarHostState = snackbarHostState)

                    val isFromAppInfo =
                        intent.action == android.content.Intent.ACTION_APPLICATION_PREFERENCES
                    val expandPlayer = intent.getBooleanExtra("expand_player", false)
                    var showSettings by rememberSaveable { mutableStateOf(isFromAppInfo) }

                    Box(modifier = Modifier.fillMaxSize()) {

                        if (showSettings) {

                            var currentSettingsScreen by rememberSaveable {
                                mutableStateOf(
                                    SettingsDestination.MainList
                                )
                            }
                            val settingsViewModel: SettingsViewModel = koinViewModel()


                            BackHandler(enabled = currentSettingsScreen == SettingsDestination.MainList) {
                                if (isFromAppInfo) finish() else showSettings = false
                            }

                            BackHandler(enabled = currentSettingsScreen == SettingsDestination.About) {
                                currentSettingsScreen = SettingsDestination.MainList
                            }

                            Crossfade(
                                targetState = currentSettingsScreen,
                                label = "SettingsNav"
                            ) { screen ->
                                when (screen) {
                                    SettingsDestination.MainList -> {
                                        SettingsScreen(
                                            viewModel = settingsViewModel,
                                            onNavigateToAbout = {
                                                currentSettingsScreen = SettingsDestination.About
                                            },
                                            onBackClick = {
                                                if (isFromAppInfo) finish() else showSettings =
                                                    false
                                            }
                                        )
                                    }

                                    SettingsDestination.About -> {
                                        AboutScreen(
                                            onBackClick = {
                                                currentSettingsScreen = SettingsDestination.MainList
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            PlaylistScreen(
                                viewModel = viewModel,
                                onSettingsClick = { showSettings = true },
                                expandPlayer = expandPlayer,
                            )
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 80.dp)
                        )
                    }
                }
            }
        }
    }
}