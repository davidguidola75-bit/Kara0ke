package com.example.ui.screens

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.KaraokeSong
import com.example.ui.KaraokeViewModel
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.LyricsDisplay
import com.example.ui.components.MicrophoneControlCard
import com.example.ui.components.PlayerControls
import com.example.ui.components.VoiceEffectsAndEqCard
import androidx.compose.material.icons.filled.GraphicEq
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.StageSurface

@Composable
fun KaraokePlayerScreen(
    viewModel: KaraokeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecordings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val micDetected by viewModel.micDetected.collectAsStateWithLifecycle()
    val micDeviceName by viewModel.micDeviceName.collectAsStateWithLifecycle()
    val micRmsLevel by viewModel.micLevelRms.collectAsStateWithLifecycle()
    val detectedNote by viewModel.detectedNote.collectAsStateWithLifecycle()
    val isMicMonitoring by viewModel.isMicMonitoring.collectAsStateWithLifecycle()

    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showMicCard by remember { mutableStateOf(false) }
    var showFxEqCard by remember { mutableStateOf(false) }

    // Audio file picker from storage
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importLocalFile(it) }
    }

    // Permission launcher for Recording & Microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.refreshMicDevices()
            viewModel.showMessage("Permiso de micrófono concedido")
        } else {
            viewModel.showMessage("Se necesita permiso de micrófono para cantar y grabar")
        }
    }

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val stageTheme = settings.selectedTheme
    val hasVideo = currentSong?.isVideo == true && !currentSong?.mediaUrl.isNullOrBlank()
    val renderVideo = hasVideo && settings.showVideoIfAvailable

    Scaffold(
        snackbarHost = { SnackbarHostState() },
        modifier = modifier.fillMaxSize(),
        containerColor = stageTheme.backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stage Background: Either Video SurfaceView or Atmospheric Gradient
            if (renderVideo) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).apply {
                            holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    viewModel.attachVideoSurface(holder)
                                }
                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    viewModel.attachVideoSurface(null)
                                }
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("karaoke_video_surface")
                )
                // Video semi-transparent dark overlay for high contrast with lyrics
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                )
            } else {
                // Background Gradient with stage lighting
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    stageTheme.backgroundColor,
                                    stageTheme.secondaryColor,
                                    stageTheme.backgroundColor
                                )
                            )
                        )
                )
            }

            // Main Content Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar: Song Title + Artist + Quick Actions
                TopBar(
                    song = currentSong,
                    hasVideo = hasVideo,
                    showVideo = settings.showVideoIfAvailable,
                    recordingsCount = recordings.size,
                    isMicActive = isMicMonitoring,
                    isFxEqActive = showFxEqCard,
                    onOpenSearch = onNavigateToSearch,
                    onOpenSettings = onNavigateToSettings,
                    onOpenRecordings = onNavigateToRecordings,
                    onOpenFilePicker = { filePicker.launch(arrayOf("audio/*", "video/*")) },
                    onToggleMicCard = {
                        showMicCard = !showMicCard
                        if (showMicCard) showFxEqCard = false
                    },
                    onToggleFxEqCard = {
                        showFxEqCard = !showFxEqCard
                        if (showFxEqCard) showMicCard = false
                    },
                    onToggleVideo = { viewModel.toggleShowVideo(!settings.showVideoIfAvailable) }
                )

                // Visualizer Bar (when video is not covering full attention)
                AudioVisualizer(
                    isPlaying = isPlaying,
                    micLevel = micRmsLevel,
                    primaryColor = stageTheme.accentColor,
                    secondaryColor = NeonCyan,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Expandable Microphone Controls Panel
                AnimatedVisibility(visible = showMicCard) {
                    MicrophoneControlCard(
                        isMicDetected = micDetected,
                        micDeviceName = micDeviceName,
                        micRmsLevel = micRmsLevel,
                        detectedNote = detectedNote,
                        autoTuneLevel = settings.autoTuneLevel,
                        isAutoTuneActive = settings.isAutoTuneActive,
                        micGain = settings.micGain,
                        isMonitoringActive = isMicMonitoring,
                        onAutoTuneLevelChange = viewModel::setAutoTuneLevel,
                        onToggleAutoTune = viewModel::toggleAutoTune,
                        onGainChange = viewModel::setMicGain,
                        onToggleMonitoring = viewModel::toggleMicMonitoring,
                        onOpenVoiceEffectsAndEq = {
                            showFxEqCard = true
                            showMicCard = false
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Expandable Voice Effects and Graphic Equalizer Panel
                AnimatedVisibility(visible = showFxEqCard) {
                    VoiceEffectsAndEqCard(
                        effectsConfig = settings.voiceEffects,
                        eqConfig = settings.micEqualizer,
                        onVoiceEffectChange = viewModel::setVoiceEffect,
                        onVoiceEffectIntensityChange = viewModel::setVoiceEffectIntensity,
                        onToggleEcho = viewModel::toggleEcho,
                        onEchoIntensityChange = viewModel::setEchoIntensity,
                        onEchoDelayChange = viewModel::setEchoDelay,
                        onToggleReverb = viewModel::toggleReverb,
                        onReverbIntensityChange = viewModel::setReverbIntensity,
                        onReverbRoomSizeChange = viewModel::setReverbRoomSize,
                        onToggleEqualizer = viewModel::toggleEqualizer,
                        onBandGainChange = viewModel::setEqBandGain,
                        onEqPresetChange = viewModel::setEqPreset,
                        onResetEq = viewModel::resetEqualizer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Center Stage: Synchronized Lyrics
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LyricsDisplay(
                        lyrics = currentSong?.parsedLyrics ?: emptyList(),
                        activeLineIndex = activeLyricIndex,
                        settings = settings,
                        onSeekTo = viewModel::seekTo,
                        onSearchLyricsOnline = {
                            currentSong?.let { s ->
                                viewModel.onSearchQueryChanged("${s.title} ${s.artist}")
                                onNavigateToSearch()
                            }
                        }
                    )
                }

                // Bottom Karaoke Player Controls
                PlayerControls(
                    isPlaying = isPlaying,
                    currentPosMs = currentPosMs,
                    durationMs = durationMs,
                    settings = settings,
                    isRecording = isRecording,
                    recordingSeconds = recordingSeconds,
                    hasVideoAvailable = hasVideo,
                    showVideo = settings.showVideoIfAvailable,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onStop = viewModel::stop,
                    onPrevious = viewModel::playPrevious,
                    onNext = viewModel::playNext,
                    onSeekTo = viewModel::seekTo,
                    onVocalModeChange = viewModel::setVocalSuppressionMode,
                    onIntensityChange = viewModel::setVocalSuppressionIntensity,
                    onToggleRecording = viewModel::toggleLiveRecording,
                    onToggleVideoView = viewModel::toggleShowVideo
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    song: KaraokeSong?,
    hasVideo: Boolean,
    showVideo: Boolean,
    recordingsCount: Int,
    isMicActive: Boolean,
    isFxEqActive: Boolean,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onToggleMicCard: () -> Unit,
    onToggleFxEqCard: () -> Unit,
    onToggleVideo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song Info Pill
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = StageSurface.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NeonPink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (song?.isVideo == true) Icons.Default.Videocam else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: "Selecciona una canción",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song?.artist ?: "Karaoke Studio",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Voice Effects & Graphic EQ Button
        IconButton(
            onClick = onToggleFxEqCard,
            modifier = Modifier
                .size(42.dp)
                .testTag("top_bar_fx_eq_btn")
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Efectos de Voz y Ecualizador",
                tint = if (isFxEqActive) NeonPink else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Mic Toggle Button
        IconButton(
            onClick = onToggleMicCard,
            modifier = Modifier
                .size(42.dp)
                .testTag("top_bar_mic_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Micrófono y Autotune",
                tint = if (isMicActive) NeonYellow else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Import Local Song
        IconButton(
            onClick = onOpenFilePicker,
            modifier = Modifier
                .size(42.dp)
                .testTag("top_bar_import_btn")
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Cargar audio del storage",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Search & Playlist
        IconButton(
            onClick = onOpenSearch,
            modifier = Modifier
                .size(42.dp)
                .testTag("top_bar_search_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar canciones",
                tint = NeonCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        // Recordings Badge
        IconButton(
            onClick = onOpenRecordings,
            modifier = Modifier
                .size(42.dp)
                .testTag("top_bar_recordings_btn")
        ) {
            if (recordingsCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = NeonPink) {
                            Text(text = "$recordingsCount", fontSize = 10.sp)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Grabaciones",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "Grabaciones",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Customization & Settings
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(42.dp)
                .testTag("top_bar_settings_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Personalización",
                tint = NeonYellow,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
