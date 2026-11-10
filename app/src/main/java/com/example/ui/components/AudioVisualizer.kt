package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    micLevel: Float,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "visualizer_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = 36
        val barSpacing = width / barCount
        val barWidth = barSpacing * 0.55f

        val baseActivity = if (isPlaying) 0.65f else 0.15f
        val totalActivity = (baseActivity + micLevel * 0.85f).coerceIn(0.1f, 1.3f)

        for (i in 0 until barCount) {
            val x = i * barSpacing + barSpacing / 2f
            val wave1 = sin(phase + i * 0.35f)
            val wave2 = sin(phase * 1.5f + i * 0.5f)
            val normalizedHeight = ((wave1 * 0.5f + wave2 * 0.5f + 1f) / 2f) * totalActivity
            val barH = (normalizedHeight * (height * 0.85f)).coerceAtLeast(6f)

            val barTop = centerY - barH / 2f
            val barBottom = centerY + barH / 2f

            val brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor),
                startY = barTop,
                endY = barBottom
            )

            drawLine(
                brush = brush,
                start = Offset(x, barTop),
                end = Offset(x, barBottom),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
