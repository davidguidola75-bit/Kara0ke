package com.example.data.model

import android.net.Uri

data class KaraokeSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val mediaUri: Uri? = null,
    val mediaUrl: String? = null,
    val isVideo: Boolean = false,
    val isLocal: Boolean = false,
    val rawLrc: String = "",
    val parsedLyrics: List<LyricLine> = emptyList(),
    val coverUrl: String? = null,
    val instrumentalUrl: String? = null
)
