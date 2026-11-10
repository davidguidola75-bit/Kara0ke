package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KaraokeSettings
import com.example.data.model.LyricLine

@Composable
fun LyricsDisplay(
    lyrics: List<LyricLine>,
    activeLineIndex: Int,
    settings: KaraokeSettings,
    onSeekTo: (Long) -> Unit,
    onSearchLyricsOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in lyrics.indices) {
            val targetScroll = (activeLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    if (lyrics.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Sin letra sincronizada",
                    tint = settings.inactiveLyricColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No hay letra sincronizada disponible",
                    color = settings.inactiveLyricColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Puedes buscar automáticamente en la base de datos de letras online gratuita",
                    color = settings.inactiveLyricColor.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onSearchLyricsOnline,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = settings.activeLyricColor
                    ),
                    modifier = Modifier.testTag("search_lyrics_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Buscar Letra Sincronizada")
                }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("lyrics_list"),
                contentPadding = PaddingValues(vertical = 120.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeLineIndex
                    val isPast = index < activeLineIndex

                    val textColor by animateColorAsState(
                        targetValue = if (isActive) settings.activeLyricColor
                        else if (isPast) settings.inactiveLyricColor.copy(alpha = 0.55f)
                        else settings.inactiveLyricColor,
                        animationSpec = tween(durationMillis = 200),
                        label = "lyric_color"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1.08f else 1.0f,
                        animationSpec = tween(durationMillis = 250),
                        label = "lyric_scale"
                    )

                    val fontSize = if (isActive) settings.lyricSize.activeSp.sp else settings.lyricSize.inactiveSp.sp

                    val activeBg = if (isActive) {
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                settings.activeLyricColor.copy(alpha = 0.16f),
                                settings.activeLyricColor.copy(alpha = 0.22f),
                                settings.activeLyricColor.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeBg)
                            .clickable { onSeekTo(line.timestampMs) }
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                            .testTag("lyric_line_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = line.text,
                            color = textColor,
                            fontSize = fontSize,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            lineHeight = (fontSize.value * 1.3f).sp
                        )
                    }
                }
            }

            // Subtle top and bottom fades for stage depth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )
        }
    }
}
