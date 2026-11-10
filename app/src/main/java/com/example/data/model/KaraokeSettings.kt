package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class LyricSize(val label: String, val activeSp: Float, val inactiveSp: Float) {
    SMALL("Pequeña", 18f, 14f),
    MEDIUM("Mediana", 24f, 18f),
    LARGE("Grande", 32f, 22f)
}

enum class VocalSuppressionMode(val title: String, val description: String) {
    OFF("Voz Original", "Audio estéreo estándar sin procesar"),
    CENTER_CUT("Cancelación Central (L-R)", "Invierte fase estéreo para suprimir voces centradas"),
    VOCAL_BAND_EQ("Filtro Ecualizador", "Atenúa frecuencias vocales 300Hz-3.4kHz y realza graves"),
    MAX_KARAOKE("Karaoke Extremo", "Combinación de supresión de fase y ecualización de voz")
}

data class StageTheme(
    val id: String,
    val name: String,
    val backgroundColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
)

val DEFAULT_STAGE_THEMES = listOf(
    StageTheme("obsidian", "Escenario Neón", Color(0xFF0F081D), Color(0xFF23113D), Color(0xFFFF007A)),
    StageTheme("midnight", "Azul Medianoche", Color(0xFF0A1128), Color(0xFF1C2541), Color(0xFF00F0FF)),
    StageTheme("cyber", "Cyberpunk Gold", Color(0xFF120E05), Color(0xFF2E240D), Color(0xFFFFD700)),
    StageTheme("disco", "Disco Retro", Color(0xFF180A1F), Color(0xFF381445), Color(0xFF00FF9D)),
    StageTheme("pitch_black", "Negro Alto Contraste", Color(0xFF050505), Color(0xFF1A1A1A), Color(0xFFFFFFFF))
)

enum class VoiceEffectType(val label: String, val description: String) {
    NONE("Natural", "Sin alteración de tono"),
    CHIPMUNK("Ardilla / Helio", "Tono agudo y divertido"),
    MONSTER("Monstruo / Grave", "Voz profunda y resonante"),
    ROBOT("Robot / Cyborg", "Modulación cibernética futurista"),
    MEGAPHONE("Megáfono", "Efecto radio antigua / banda estrecha"),
    ALIEN("Extraterrestre", "Modulación oscilante espacial")
}

data class VoiceEffectsConfig(
    val selectedEffect: VoiceEffectType = VoiceEffectType.NONE,
    val effectIntensity: Float = 0.75f, // 0.0f to 1.0f
    val isEchoEnabled: Boolean = false,
    val echoIntensity: Float = 0.45f, // 0.0f to 1.0f
    val echoDelayMs: Int = 220, // 100ms to 500ms
    val isReverbEnabled: Boolean = false,
    val reverbIntensity: Float = 0.50f, // 0.0f to 1.0f
    val reverbRoomSize: Float = 0.65f // 0.1f to 1.0f
)

enum class EqPreset(val label: String, val gains: List<Float>) {
    FLAT("Plano (Natural)", listOf(0f, 0f, 0f, 0f, 0f)),
    VOCAL_CLARITY("Voz Cristalina", listOf(-2f, -1f, 3f, 6f, 4f)),
    WARMTH("Calidez & Cuerpo", listOf(2f, 5f, 2f, -1f, -2f)),
    PUNCH("Potencia / Escenario", listOf(4f, 2f, 4f, 5f, 3f)),
    LOW_CUT("Anti-viento / Filtro Pop", listOf(-9f, -4f, 1f, 3f, 2f))
}

data class MicEqualizerConfig(
    val isEnabled: Boolean = true,
    // Frequencies: 60Hz (Graves), 250Hz (Calidez), 1kHz (Presencia), 4kHz (Claridad), 12kHz (Brillo)
    val bandGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f), // in dB (-12 to +12)
    val selectedPreset: EqPreset = EqPreset.FLAT
)

data class KaraokeSettings(
    val activeLyricColor: Color = Color(0xFFFFEB3B), // Vibrant Yellow
    val inactiveLyricColor: Color = Color(0xFFB0B0C8), // Muted Soft Lilac/Silver
    val lyricSize: LyricSize = LyricSize.MEDIUM,
    val selectedTheme: StageTheme = DEFAULT_STAGE_THEMES[0],
    val autoTuneLevel: Int = 5, // 1 to 10
    val isAutoTuneActive: Boolean = true,
    val micGain: Float = 1.3f, // 0.0f to 2.5f
    val isMicMonitoringActive: Boolean = false,
    val vocalSuppressionMode: VocalSuppressionMode = VocalSuppressionMode.OFF,
    val vocalSuppressionIntensity: Float = 0.85f, // 0.0f to 1.0f
    val showVideoIfAvailable: Boolean = true,
    val voiceEffects: VoiceEffectsConfig = VoiceEffectsConfig(),
    val micEqualizer: MicEqualizerConfig = MicEqualizerConfig()
)

data class RecordingItem(
    val id: String,
    val songTitle: String,
    val dateFormatted: String,
    val durationSeconds: Int,
    val filePath: String,
    val fileSizeBytes: Long = 0L
)
