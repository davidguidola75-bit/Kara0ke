package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KaraokeViewModel
import com.example.ui.screens.KaraokePlayerScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.SearchPlaylistScreen
import com.example.ui.screens.SettingsCustomizationScreen
import com.example.ui.theme.KaraokeStudioTheme

enum class KaraokeScreenDestination {
    PLAYER,
    SEARCH_PLAYLIST,
    SETTINGS,
    RECORDINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaraokeStudioTheme {
                KaraokeAppRoot()
            }
        }
    }
}

@Composable
fun KaraokeAppRoot(
    viewModel: KaraokeViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(KaraokeScreenDestination.PLAYER) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition",
        modifier = Modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            KaraokeScreenDestination.PLAYER -> {
                KaraokePlayerScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { currentScreen = KaraokeScreenDestination.SEARCH_PLAYLIST },
                    onNavigateToSettings = { currentScreen = KaraokeScreenDestination.SETTINGS },
                    onNavigateToRecordings = { currentScreen = KaraokeScreenDestination.RECORDINGS }
                )
            }
            KaraokeScreenDestination.SEARCH_PLAYLIST -> {
                SearchPlaylistScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = KaraokeScreenDestination.PLAYER }
                )
            }
            KaraokeScreenDestination.SETTINGS -> {
                SettingsCustomizationScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = KaraokeScreenDestination.PLAYER }
                )
            }
            KaraokeScreenDestination.RECORDINGS -> {
                RecordingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = KaraokeScreenDestination.PLAYER }
                )
            }
        }
    }
}
