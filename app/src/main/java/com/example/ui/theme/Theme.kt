package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KaraokeDarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    primaryContainer = NeonPurple,
    onPrimaryContainer = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = StageSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = NeonYellow,
    onTertiary = Color.Black,
    background = StageDarkBg,
    onBackground = TextPrimary,
    surface = StageSurface,
    onSurface = TextPrimary,
    surfaceVariant = StageSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StageSurfaceBorder
)

@Composable
fun KaraokeStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KaraokeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
