package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqPreset
import com.example.data.model.MicEqualizerConfig
import com.example.data.model.VoiceEffectType
import com.example.data.model.VoiceEffectsConfig
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceVariant

@Composable
fun VoiceEffectsAndEqCard(
    effectsConfig: VoiceEffectsConfig,
    eqConfig: MicEqualizerConfig,
    onVoiceEffectChange: (VoiceEffectType) -> Unit,
    onVoiceEffectIntensityChange: (Float) -> Unit,
    onToggleEcho: (Boolean) -> Unit,
    onEchoIntensityChange: (Float) -> Unit,
    onEchoDelayChange: (Int) -> Unit,
    onToggleReverb: (Boolean) -> Unit,
    onReverbIntensityChange: (Float) -> Unit,
    onReverbRoomSizeChange: (Float) -> Unit,
    onToggleEqualizer: (Boolean) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onEqPresetChange: (EqPreset) -> Unit,
    onResetEq: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_effects_and_eq_card"),
        colors = CardDefaults.cardColors(containerColor = StageSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Tabs: Efectos de Voz vs Ecualizador Gráfico
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = StageSurfaceVariant,
                contentColor = NeonPink,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonPink
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .testTag("fx_eq_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) NeonPink else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Efectos de Voz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) NeonCyan else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Ecualizador Gráfico",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                VoiceEffectsSection(
                    config = effectsConfig,
                    onVoiceEffectChange = onVoiceEffectChange,
                    onVoiceEffectIntensityChange = onVoiceEffectIntensityChange,
                    onToggleEcho = onToggleEcho,
                    onEchoIntensityChange = onEchoIntensityChange,
                    onEchoDelayChange = onEchoDelayChange,
                    onToggleReverb = onToggleReverb,
                    onReverbIntensityChange = onReverbIntensityChange,
                    onReverbRoomSizeChange = onReverbRoomSizeChange
                )
            } else {
                GraphicEqualizerSection(
                    config = eqConfig,
                    onToggleEqualizer = onToggleEqualizer,
                    onBandGainChange = onBandGainChange,
                    onEqPresetChange = onEqPresetChange,
                    onResetEq = onResetEq
                )
            }
        }
    }
}

@Composable
private fun VoiceEffectsSection(
    config: VoiceEffectsConfig,
    onVoiceEffectChange: (VoiceEffectType) -> Unit,
    onVoiceEffectIntensityChange: (Float) -> Unit,
    onToggleEcho: (Boolean) -> Unit,
    onEchoIntensityChange: (Float) -> Unit,
    onEchoDelayChange: (Int) -> Unit,
    onToggleReverb: (Boolean) -> Unit,
    onReverbIntensityChange: (Float) -> Unit,
    onReverbRoomSizeChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Cambiador de Voz (Voice Changer)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StageSurfaceVariant.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cambiador de Voz",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = config.selectedEffect.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPink
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VoiceEffectType.values()) { effect ->
                    val isSelected = config.selectedEffect == effect
                    FilterChip(
                        selected = isSelected,
                        onClick = { onVoiceEffectChange(effect) },
                        label = {
                            Text(
                                text = effect.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPink,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_effect_${effect.name.lowercase()}")
                    )
                }
            }

            if (config.selectedEffect != VoiceEffectType.NONE) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intensidad del efecto: ${(config.effectIntensity * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Slider(
                    value = config.effectIntensity,
                    onValueChange = onVoiceEffectIntensityChange,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPink,
                        activeTrackColor = NeonPink,
                        inactiveTrackColor = StageSurface
                    ),
                    modifier = Modifier.testTag("slider_effect_intensity")
                )
            }
        }

        // 2. Efecto de Eco (Echo)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StageSurfaceVariant.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Efecto de Eco",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Switch(
                    checked = config.isEchoEnabled,
                    onCheckedChange = onToggleEcho,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("switch_echo")
                )
            }

            AnimatedVisibility(visible = config.isEchoEnabled) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensidad de Eco: ${(config.echoIntensity * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Retardo: ${config.echoDelayMs} ms",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = config.echoIntensity,
                        onValueChange = onEchoIntensityChange,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = StageSurface
                        ),
                        modifier = Modifier.testTag("slider_echo_intensity")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiempo de repetición",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Slider(
                        value = config.echoDelayMs.toFloat(),
                        onValueChange = { onEchoDelayChange(it.toInt()) },
                        valueRange = 100f..500f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = StageSurface
                        ),
                        modifier = Modifier.testTag("slider_echo_delay")
                    )
                }
            }
        }

        // 3. Efecto de Reverberación (Reverb)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StageSurfaceVariant.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SurroundSound,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reverberación de Sala",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Switch(
                    checked = config.isReverbEnabled,
                    onCheckedChange = onToggleReverb,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonYellow,
                        checkedTrackColor = NeonYellow.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("switch_reverb")
                )
            }

            AnimatedVisibility(visible = config.isReverbEnabled) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intensidad de Reverb: ${(config.reverbIntensity * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Acústica: ${(config.reverbRoomSize * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = NeonYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = config.reverbIntensity,
                        onValueChange = onReverbIntensityChange,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonYellow,
                            activeTrackColor = NeonYellow,
                            inactiveTrackColor = StageSurface
                        ),
                        modifier = Modifier.testTag("slider_reverb_intensity")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tamaño de Sala / Espacio Escénico",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Slider(
                        value = config.reverbRoomSize,
                        onValueChange = onReverbRoomSizeChange,
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonYellow,
                            activeTrackColor = NeonYellow,
                            inactiveTrackColor = StageSurface
                        ),
                        modifier = Modifier.testTag("slider_reverb_room")
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphicEqualizerSection(
    config: MicEqualizerConfig,
    onToggleEqualizer: (Boolean) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onEqPresetChange: (EqPreset) -> Unit,
    onResetEq: () -> Unit
) {
    val bandLabels = listOf(
        Pair("60 Hz", "Sub-Graves"),
        Pair("250 Hz", "Calidez"),
        Pair("1 kHz", "Presencia"),
        Pair("4 kHz", "Claridad"),
        Pair("12 kHz", "Brillo")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Equalizer Header with Toggle & Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (config.isEnabled) "EQ Activo (5 Bandas)" else "EQ en Bypass (Desactivado)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (config.isEnabled) NeonCyan else Color.White.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onResetEq,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_reset_eq")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Restablecer EQ",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = onToggleEqualizer,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("switch_equalizer")
                )
            }
        }

        // Presets Chips
        Text(
            text = "Presets Vocales:",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(EqPreset.values()) { preset ->
                val isSelected = config.selectedPreset == preset && config.isEnabled
                FilterChip(
                    selected = isSelected,
                    onClick = { onEqPresetChange(preset) },
                    label = {
                        Text(
                            text = preset.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("chip_preset_${preset.name.lowercase()}")
                )
            }
        }

        // Graphic EQ Interactive Frequency Response Visualizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            colors = CardDefaults.cardColors(containerColor = StageSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(68.dp)) {
                    val w = size.width
                    val h = size.height
                    val zeroY = h / 2f

                    // Draw 0dB reference line
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(0f, zeroY),
                        end = Offset(w, zeroY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw frequency curve
                    val gains = config.bandGains
                    val path = Path()
                    val bandCount = 5

                    val points = mutableListOf<Offset>()
                    for (i in 0 until bandCount) {
                        val x = (i + 0.5f) * (w / bandCount)
                        val gainDb = if (config.isEnabled && i in gains.indices) gains[i] else 0f
                        // Map -12dB..+12dB to h..0
                        val y = zeroY - (gainDb / 12f) * (h * 0.42f)
                        points.add(Offset(x, y))
                    }

                    if (points.isNotEmpty()) {
                        path.moveTo(0f, zeroY)
                        path.lineTo(points[0].x * 0.5f, points[0].y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                        path.lineTo(w, points.last().y)

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink, NeonYellow)),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw node points
                        for (p in points) {
                            drawCircle(
                                color = NeonYellow,
                                radius = 4.dp.toPx(),
                                center = p
                            )
                        }
                    }
                }
            }
        }

        // 5 Sliders for each frequency band
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            bandLabels.forEachIndexed { index, (freq, description) ->
                val gain = if (index in config.bandGains.indices) config.bandGains[index] else 0f
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StageSurfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = freq,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.width(54.dp)
                            )
                            Text(
                                text = description,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "${if (gain > 0) "+" else ""}${gain.toInt()} dB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gain > 0) NeonGreen else if (gain < 0) NeonPink else Color.White
                        )
                    }

                    Slider(
                        value = gain,
                        onValueChange = { onBandGainChange(index, it) },
                        valueRange = -12f..12f,
                        enabled = config.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = StageSurface
                        ),
                        modifier = Modifier.testTag("slider_eq_band_$index")
                    )
                }
            }
        }
    }
}
