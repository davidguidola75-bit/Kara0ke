package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import com.example.ui.components.VoiceEffectsAndEqCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DEFAULT_STAGE_THEMES
import com.example.data.model.LyricSize
import com.example.data.model.StageTheme
import com.example.ui.KaraokeViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.StageDarkBg
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceVariant

private val ACTIVE_COLOR_OPTIONS = listOf(
    Color(0xFFFFEB3B), // Neon Yellow
    Color(0xFF00F0FF), // Electric Cyan
    Color(0xFFFF007A), // Neon Pink
    Color(0xFF00FF88), // Emerald Green
    Color(0xFFFF9100), // Vibrant Orange
    Color(0xFFFFFFFF)  // Pure White
)

private val INACTIVE_COLOR_OPTIONS = listOf(
    Color(0xFFB4ADCD), // Soft Lilac Silver
    Color(0xFF888899), // Muted Grey
    Color(0xFF555566), // Dark Slate
    Color(0xFFE0E0E0), // Soft White
    Color(0xFF0088AA)  // Deep Cyan
)

@Composable
fun SettingsCustomizationScreen(
    viewModel: KaraokeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = StageDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Personalización de Karaoke",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Live Preview Card
                Text(
                    text = "VISTA PREVIA EN VIVO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lyrics_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = settings.selectedTheme.backgroundColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "♪ Línea anterior en curso ♪",
                            color = settings.inactiveLyricColor.copy(alpha = 0.5f),
                            fontSize = settings.lyricSize.inactiveSp.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(settings.activeLyricColor.copy(alpha = 0.18f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "¡Bésame, bésame mucho!",
                                color = settings.activeLyricColor,
                                fontSize = settings.lyricSize.activeSp.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Como si fuera esta noche la última vez...",
                            color = settings.inactiveLyricColor,
                            fontSize = settings.lyricSize.inactiveSp.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 1. Color de Letra Activa
                CustomizationSection(
                    title = "Color de Letra Activa (La que estás cantando)",
                    icon = Icons.Default.Palette
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ACTIVE_COLOR_OPTIONS.forEach { color ->
                            ColorCircle(
                                color = color,
                                isSelected = settings.activeLyricColor == color,
                                onClick = { viewModel.setActiveLyricColor(color) }
                            )
                        }
                    }
                }

                // 2. Color de Letra Pasiva
                CustomizationSection(
                    title = "Color de Letra Pasiva (Fondo / Próximas líneas)",
                    icon = Icons.Default.Palette
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        INACTIVE_COLOR_OPTIONS.forEach { color ->
                            ColorCircle(
                                color = color,
                                isSelected = settings.inactiveLyricColor == color,
                                onClick = { viewModel.setInactiveLyricColor(color) }
                            )
                        }
                    }
                }

                // 3. Tamaño de la Letra
                CustomizationSection(
                    title = "Tamaño de la Letra de Karaoke",
                    icon = Icons.Default.FormatSize
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LyricSize.values().forEach { size ->
                            val isSelected = settings.lyricSize == size
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setLyricSize(size) },
                                label = {
                                    Text(
                                        text = "${size.label} (${size.activeSp.toInt()}sp)",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPink,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. Color de Fondo / Escenario (Contraste)
                CustomizationSection(
                    title = "Color de Fondo del Escenario (Alto Contraste)",
                    icon = Icons.Default.Palette
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DEFAULT_STAGE_THEMES.forEach { theme ->
                            val isSelected = settings.selectedTheme.id == theme.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setStageTheme(theme) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) StageSurfaceVariant else StageSurface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(theme.backgroundColor)
                                            .border(1.dp, theme.accentColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = theme.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionado",
                                            tint = NeonCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Autotune y Ganancia de Micrófono
                CustomizationSection(
                    title = "Autotune y Micrófono",
                    icon = Icons.Default.Tune
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rango de Autotune: ${settings.autoTuneLevel} de 10",
                                color = NeonYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = settings.autoTuneLevel.toFloat(),
                            onValueChange = { viewModel.setAutoTuneLevel(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonYellow,
                                activeTrackColor = NeonYellow
                            ),
                            modifier = Modifier.testTag("settings_autotune_slider")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ganancia de Micrófono: ${(settings.micGain * 100).toInt()}%",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = settings.micGain,
                            onValueChange = viewModel::setMicGain,
                            valueRange = 0.2f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("settings_gain_slider")
                        )
                    }
                }

                // 6. Efectos de Voz y Ecualizador Gráfico
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
                    onResetEq = viewModel::resetEqualizer
                )

                // 7. Video automático
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StageSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = NeonPink
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Modo Video Automático",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Reproducir video de fondo cuando esté disponible",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.showVideoIfAvailable,
                            onCheckedChange = viewModel::toggleShowVideo,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonPink,
                                checkedTrackColor = NeonPink.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizationSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StageSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonPink,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) NeonPink else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Color seleccionado",
                tint = if (color == Color.White || color == Color(0xFFFFEB3B)) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
