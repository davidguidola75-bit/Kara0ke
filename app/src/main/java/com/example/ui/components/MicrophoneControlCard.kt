package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceVariant

@Composable
fun MicrophoneControlCard(
    isMicDetected: Boolean,
    micDeviceName: String,
    micRmsLevel: Float,
    detectedNote: String,
    autoTuneLevel: Int,
    isAutoTuneActive: Boolean,
    micGain: Float,
    isMonitoringActive: Boolean,
    onAutoTuneLevelChange: (Int) -> Unit,
    onToggleAutoTune: (Boolean) -> Unit,
    onGainChange: (Float) -> Unit,
    onToggleMonitoring: () -> Unit,
    onOpenVoiceEffectsAndEq: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("microphone_control_card"),
        colors = CardDefaults.cardColors(containerColor = StageSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Mic Device Name + Status Pill + Activation Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isMonitoringActive) NeonGreen.copy(alpha = 0.2f) else StageSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMonitoringActive) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Micrófono",
                            tint = if (isMonitoringActive) NeonGreen else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isMicDetected) micDeviceName else "Micrófono no detectado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isMonitoringActive) "Micrófono Activo • Retorno listo" else "Toca activar para cantar",
                            fontSize = 11.sp,
                            color = if (isMonitoringActive) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onToggleMonitoring,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoringActive) NeonGreen else NeonPink,
                        contentColor = if (isMonitoringActive) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("btn_toggle_mic")
                ) {
                    Text(
                        text = if (isMonitoringActive) "Activo" else "Activar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isMonitoringActive) {
                Spacer(modifier = Modifier.height(14.dp))

                // VU Meter & Detected Musical Pitch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nivel:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.width(40.dp)
                    )
                    LinearProgressIndicator(
                        progress = { micRmsLevel },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("mic_vu_meter"),
                        color = if (micRmsLevel > 0.8f) Color.Red else NeonGreen,
                        trackColor = StageSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StageSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tono: $detectedNote",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // AutoTune Section (Ranges 1 to 10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AutoTune (Nivel $autoTuneLevel / 10)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Switch(
                        checked = isAutoTuneActive,
                        onCheckedChange = onToggleAutoTune,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("switch_autotune")
                    )
                }

                if (isAutoTuneActive) {
                    Text(
                        text = when (autoTuneLevel) {
                            in 1..3 -> "Efecto sutil: afinación natural de notas"
                            in 4..7 -> "Efecto pop comercial: corrección precisa de tono"
                            else -> "Efecto robótico: estilo T-Pain / Daft Punk"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("autotune_levels_row")
                    ) {
                        items((1..10).toList()) { level ->
                            val isSelected = level == autoTuneLevel
                            FilterChip(
                                selected = isSelected,
                                onClick = { onAutoTuneLevelChange(level) },
                                label = {
                                    Text(
                                        text = "$level",
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.testTag("chip_autotune_$level")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mic Gain Slider (0.1x to 2.5x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ganancia de Micrófono:",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${(micGain * 100).toInt()}% (+${((micGain - 1f) * 12).toInt()} dB)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonYellow
                    )
                }

                Slider(
                    value = micGain,
                    onValueChange = onGainChange,
                    valueRange = 0.2f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonYellow,
                        activeTrackColor = NeonYellow,
                        inactiveTrackColor = StageSurfaceVariant
                    ),
                    modifier = Modifier.testTag("slider_mic_gain")
                )

                Spacer(modifier = Modifier.height(10.dp))

                androidx.compose.material3.OutlinedButton(
                    onClick = onOpenVoiceEffectsAndEq,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_fx_and_eq"),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonPink
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Efectos de Voz y Ecualizador Gráfico",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
