package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KaraokeSettings
import com.example.data.model.VocalSuppressionMode
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.StageSurfaceVariant

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentPosMs: Long,
    durationMs: Long,
    settings: KaraokeSettings,
    isRecording: Boolean,
    recordingSeconds: Int,
    hasVideoAvailable: Boolean,
    showVideo: Boolean,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVocalModeChange: (VocalSuppressionMode) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onToggleRecording: () -> Unit,
    onToggleVideoView: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showVocalOptions by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val recPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Seekbar and Time labels
            val progress = if (durationMs > 0) (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentPosMs),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTime(durationMs),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Slider(
                value = progress,
                onValueChange = { frac ->
                    onSeekTo((frac * durationMs).toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_seekbar"),
                colors = SliderDefaults.colors(
                    thumbColor = NeonPink,
                    activeTrackColor = NeonPink,
                    inactiveTrackColor = StageSurfaceVariant
                )
            )

            // Primary Playback Buttons: Anterior, Stop, Play/Pause, Posterior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_previous"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Stop button
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_stop"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Detener",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Big Play / Pause button
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("btn_play_pause"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = NeonPink,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_next"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Posterior",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Video toggle if available
                if (hasVideoAvailable) {
                    IconButton(
                        onClick = { onToggleVideoView(!showVideo) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_toggle_video"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (showVideo) NeonCyan else Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Alternar Modo Video",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Karaoke Vocal Remover toggle & Live Recording button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vocal Suppression Mode Button
                val isSuppressed = settings.vocalSuppressionMode != VocalSuppressionMode.OFF
                Button(
                    onClick = { showVocalOptions = !showVocalOptions },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_vocal_remover"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuppressed) NeonCyan else StageSurfaceVariant,
                        contentColor = if (isSuppressed) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isSuppressed) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSuppressed) "Voz Suprimida" else "Voz Original",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live Recording Button
                Button(
                    onClick = onToggleRecording,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_live_record"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFD32F2F) else StageSurfaceVariant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .scale(if (isRecording) recPulse else 1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRecording) "Grabando (${recordingSeconds}s)" else "Grabar en Vivo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expandable Vocal Suppression Control Drawer
            AnimatedVisibility(visible = showVocalOptions) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = StageSurfaceVariant.copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Modo de Supresión de Voz de Cantante",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellow
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            VocalSuppressionMode.values().forEach { mode ->
                                val isSelected = settings.vocalSuppressionMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onVocalModeChange(mode) },
                                    label = {
                                        Text(
                                            text = when (mode) {
                                                VocalSuppressionMode.OFF -> "Original"
                                                VocalSuppressionMode.CENTER_CUT -> "Centro L-R"
                                                VocalSuppressionMode.VOCAL_BAND_EQ -> "EQ Voz"
                                                VocalSuppressionMode.MAX_KARAOKE -> "Máximo"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        if (settings.vocalSuppressionMode != VocalSuppressionMode.OFF) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Intensidad de cancelación: ${(settings.vocalSuppressionIntensity * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Slider(
                                value = settings.vocalSuppressionIntensity,
                                onValueChange = onIntensityChange,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
