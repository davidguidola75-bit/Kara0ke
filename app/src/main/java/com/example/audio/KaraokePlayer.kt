package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import com.example.data.model.KaraokeSong
import com.example.data.model.VocalSuppressionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KaraokePlayer(private val context: Context) {

    private val tag = "KaraokePlayer"

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isPrepared = MutableStateFlow(false)
    val isPrepared: StateFlow<Boolean> = _isPrepared.asStateFlow()

    private var currentSong: KaraokeSong? = null
    private var attachedSurfaceHolder: SurfaceHolder? = null

    var onSongCompleted: (() -> Unit)? = null

    fun attachSurface(holder: SurfaceHolder?) {
        attachedSurfaceHolder = holder
        mediaPlayer?.setDisplay(holder)
    }

    fun playSong(song: KaraokeSong, mode: VocalSuppressionMode, intensity: Float) {
        currentSong = song
        stop()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (song.mediaUri != null) {
                    setDataSource(context, song.mediaUri)
                } else if (!song.mediaUrl.isNullOrBlank()) {
                    setDataSource(song.mediaUrl)
                } else {
                    Log.e(tag, "No media URI or URL provided for song: ${song.title}")
                    return
                }

                if (attachedSurfaceHolder != null && song.isVideo) {
                    setDisplay(attachedSurfaceHolder)
                }

                setOnPreparedListener { mp ->
                    _isPrepared.value = true
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(song.durationMs)
                    setupEqualizer(mp.audioSessionId, mode, intensity)
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    onSongCompleted?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(tag, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }

                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize player: ${e.message}")
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        releaseEqualizer()
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        _isPrepared.value = false
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            val target = positionMs.coerceIn(0L, _durationMs.value)
            player.seekTo(target.toInt())
            _currentPositionMs.value = target
        }
    }

    fun applyVocalSuppression(mode: VocalSuppressionMode, intensity: Float) {
        val eq = equalizer ?: return
        try {
            when (mode) {
                VocalSuppressionMode.OFF -> {
                    // Reset to flat
                    val numBands = eq.numberOfBands
                    for (i in 0 until numBands) {
                        eq.setBandLevel(i.toShort(), 0)
                    }
                    eq.enabled = false
                }
                VocalSuppressionMode.CENTER_CUT,
                VocalSuppressionMode.VOCAL_BAND_EQ,
                VocalSuppressionMode.MAX_KARAOKE -> {
                    eq.enabled = true
                    val minLevel = eq.bandLevelRange[0]
                    val maxLevel = eq.bandLevelRange[1]
                    val numBands = eq.numberOfBands

                    for (i in 0 until numBands) {
                        val freqCenter = eq.getCenterFreq(i.toShort()) / 1000 // in Hz
                        when {
                            // Vocal mid-frequencies: 300Hz to 3500Hz -> Attenuate strongly
                            freqCenter in 300..3500 -> {
                                val cutLevel = (minLevel * intensity * 0.85f).toInt().toShort()
                                eq.setBandLevel(i.toShort(), cutLevel)
                            }
                            // Bass / Kick preservation: below 250Hz -> Boost slightly
                            freqCenter < 250 -> {
                                val bassBoost = (maxLevel * 0.4f * intensity).toInt().toShort()
                                eq.setBandLevel(i.toShort(), bassBoost)
                            }
                            // High cymbals/air: above 4000Hz -> Bright clarity
                            else -> {
                                val airBoost = (maxLevel * 0.25f * intensity).toInt().toShort()
                                eq.setBandLevel(i.toShort(), airBoost)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Equalizer adjustment failed: ${e.message}")
        }
    }

    private fun setupEqualizer(sessionId: Int, mode: VocalSuppressionMode, intensity: Float) {
        releaseEqualizer()
        try {
            equalizer = Equalizer(0, sessionId).apply {
                applyVocalSuppression(mode, intensity)
            }
        } catch (e: Exception) {
            Log.w(tag, "Cannot initialize Equalizer on session $sessionId: ${e.message}")
        }
    }

    private fun releaseEqualizer() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            _currentPositionMs.value = mp.currentPosition.toLong()
                        }
                    } catch (_: Exception) {}
                }
                delay(60) // Smooth ~16fps updates for synchronized karaoke lyrics
            }
        }
    }

    fun release() {
        stop()
    }
}
