package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.data.api.NetworkClient
import com.example.data.model.KaraokeSong
import com.example.data.model.LrcParser
import com.example.data.model.RecordingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KaraokeRepository(private val context: Context) {

    private val tag = "KaraokeRepo"

    suspend fun getInitialPlaylist(): List<KaraokeSong> = withContext(Dispatchers.IO) {
        val sampleList = SampleSongs.getDemoSongs()
        val localList = scanDeviceAudioFiles(limit = 10)
        (sampleList + localList).distinctBy { it.id }
    }

    suspend fun scanDeviceAudioFiles(limit: Int = 30): List<KaraokeSong> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<KaraokeSong>()
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                var count = 0
                while (cursor.moveToNext() && count < limit) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Pista desconocida"
                    val artist = cursor.getString(artistCol) ?: "Artista desconocido"
                    val album = cursor.getString(albumCol) ?: ""
                    val duration = cursor.getLong(durationCol)
                    val contentUri = Uri.withAppendedPath(uri, id.toString())

                    songs.add(
                        KaraokeSong(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Artista desconocido" else artist,
                            album = album,
                            durationMs = duration,
                            mediaUri = contentUri,
                            isVideo = false,
                            isLocal = true,
                            parsedLyrics = emptyList()
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Device audio scan error: ${e.message}")
        }
        songs
    }

    suspend fun loadSongFromUri(uri: Uri): KaraokeSong = withContext(Dispatchers.IO) {
        var title = "Canción seleccionada"
        var artist = "Local"
        var durationMs = 0L
        var isVideo = false

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val extractedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val extractedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val extractedDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)

            if (!extractedTitle.isNullOrBlank()) title = extractedTitle
            if (!extractedArtist.isNullOrBlank()) artist = extractedArtist
            durationMs = extractedDuration?.toLongOrNull() ?: 0L
            isVideo = hasVideo == "yes"
        } catch (e: Exception) {
            Log.w(tag, "Error extracting metadata: ${e.message}")
            title = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Audio importado"
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        val song = KaraokeSong(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            artist = artist,
            durationMs = durationMs,
            mediaUri = uri,
            isVideo = isVideo,
            isLocal = true
        )

        // Attempt online lyrics search automatically
        fetchLyricsForSong(song)
    }

    suspend fun searchInternetSongs(query: String): List<KaraokeSong> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val results = NetworkClient.lrclibApi.searchLyrics(query)
            results.map { item ->
                val track = item.trackName ?: item.name ?: query
                val artist = item.artistName ?: "Artista"
                val lrc = item.syncedLyrics ?: item.plainLyrics ?: ""
                val parsed = if (item.syncedLyrics != null) {
                    LrcParser.parse(item.syncedLyrics)
                } else if (!item.plainLyrics.isNullOrBlank()) {
                    item.plainLyrics.lines().mapIndexed { index, line ->
                        com.example.data.model.LyricLine(index * 4000L, line)
                    }
                } else emptyList()

                KaraokeSong(
                    id = "net_${item.id}_${System.currentTimeMillis()}",
                    title = track,
                    artist = artist,
                    album = item.albumName ?: "",
                    durationMs = (item.duration?.times(1000))?.toLong() ?: 180_000L,
                    rawLrc = lrc,
                    parsedLyrics = parsed,
                    isVideo = false,
                    isLocal = false,
                    mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "Internet lyrics search failed: ${e.message}")
            // Fallback matching demo songs
            SampleSongs.getDemoSongs().filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun fetchLyricsForSong(song: KaraokeSong): KaraokeSong = withContext(Dispatchers.IO) {
        if (song.parsedLyrics.isNotEmpty()) return@withContext song
        try {
            val response = NetworkClient.lrclibApi.getLyrics(song.title, song.artist)
            if (response != null && !response.syncedLyrics.isNullOrBlank()) {
                val parsed = LrcParser.parse(response.syncedLyrics)
                return@withContext song.copy(
                    rawLrc = response.syncedLyrics,
                    parsedLyrics = parsed
                )
            } else {
                // Try fuzzy search
                val searchList = NetworkClient.lrclibApi.searchLyrics("${song.title} ${song.artist}")
                val matched = searchList.firstOrNull { !it.syncedLyrics.isNullOrBlank() }
                if (matched?.syncedLyrics != null) {
                    return@withContext song.copy(
                        rawLrc = matched.syncedLyrics,
                        parsedLyrics = LrcParser.parse(matched.syncedLyrics)
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Lyrics fetch failed: ${e.message}")
        }
        song
    }

    fun getRecordings(): List<RecordingItem> {
        val dir = File(context.filesDir, "karaoke_recordings")
        if (!dir.exists()) return emptyList()

        val files = dir.listFiles { file -> file.extension in listOf("m4a", "wav", "mp3") } ?: return emptyList()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        return files.sortedByDescending { it.lastModified() }.map { file ->
            val durationSec = try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
                retriever.release()
                dur / 1000
            } catch (_: Exception) {
                0
            }
            RecordingItem(
                id = file.name,
                songTitle = file.nameWithoutExtension.replace('_', ' '),
                dateFormatted = dateFormat.format(Date(file.lastModified())),
                durationSeconds = durationSec,
                filePath = file.absolutePath,
                fileSizeBytes = file.length()
            )
        }
    }

    fun deleteRecording(recording: RecordingItem): Boolean {
        val file = File(recording.filePath)
        return if (file.exists()) file.delete() else false
    }
}
