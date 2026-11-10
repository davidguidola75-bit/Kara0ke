package com.example.ui

import android.app.Application
import android.net.Uri
import android.view.SurfaceHolder
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.KaraokePlayer
import com.example.audio.KaraokeRecorder
import com.example.audio.MicrophoneEngine
import com.example.data.model.EqPreset
import com.example.data.model.KaraokeSettings
import com.example.data.model.KaraokeSong
import com.example.data.model.LyricSize
import com.example.data.model.MicEqualizerConfig
import com.example.data.model.RecordingItem
import com.example.data.model.StageTheme
import com.example.data.model.VoiceEffectType
import com.example.data.model.VoiceEffectsConfig
import com.example.data.model.VocalSuppressionMode
import com.example.data.repository.KaraokeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchSource {
    ALL, LOCAL, INTERNET
}

class KaraokeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KaraokeRepository(application)
    private val player = KaraokePlayer(application)
    private val micEngine = MicrophoneEngine(application)
    private val recorder = KaraokeRecorder(application)

    // Playlist & Current Song
    private val _playlist = MutableStateFlow<List<KaraokeSong>>(emptyList())
    val playlist: StateFlow<List<KaraokeSong>> = _playlist.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    val currentSong: StateFlow<KaraokeSong?> = combine(_playlist, _currentIndex) { list, index ->
        if (list.isNotEmpty() && index in list.indices) list[index] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Player State
    val isPlaying = player.isPlaying
    val currentPositionMs = player.currentPositionMs
    val durationMs = player.durationMs
    val isPrepared = player.isPrepared

    // Synced Lyric Line Index
    val activeLyricIndex: StateFlow<Int> = combine(currentSong, currentPositionMs) { song, posMs ->
        if (song == null || song.parsedLyrics.isEmpty()) return@combine -1
        val lyrics = song.parsedLyrics
        var activeIdx = -1
        for (i in lyrics.indices) {
            if (posMs >= lyrics[i].timestampMs) {
                activeIdx = i
            } else {
                break
            }
        }
        activeIdx
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // Settings
    private val _settings = MutableStateFlow(KaraokeSettings())
    val settings: StateFlow<KaraokeSettings> = _settings.asStateFlow()

    // Search & Autocomplete
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchSource = MutableStateFlow(SearchSource.ALL)
    val searchSource: StateFlow<SearchSource> = _searchSource.asStateFlow()

    private val _searchResults = MutableStateFlow<List<KaraokeSong>>(emptyList())
    val searchResults: StateFlow<List<KaraokeSong>> = _searchResults.asStateFlow()

    private val _selectedSearchSongIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedSearchSongIds: StateFlow<Set<String>> = _selectedSearchSongIds.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // Microphone & Live Recording
    val micDetected = micEngine.isMicDetected
    val micDeviceName = micEngine.micDeviceName
    val micLevelRms = micEngine.micLevelRms
    val detectedNote = micEngine.detectedNote
    val isMicMonitoring = micEngine.isMonitoring

    val isRecording = recorder.isRecording
    val recordingSeconds = recorder.recordingSeconds

    private val _recordings = MutableStateFlow<List<RecordingItem>>(emptyList())
    val recordings: StateFlow<List<RecordingItem>> = _recordings.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        player.onSongCompleted = {
            playNext()
        }

        micEngine.updateAudioDevices()
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val initial = repository.getInitialPlaylist()
            _playlist.value = initial
            _recordings.value = repository.getRecordings()
            if (initial.isNotEmpty()) {
                loadSong(0, autoPlay = false)
            }
        }
    }

    fun attachVideoSurface(holder: SurfaceHolder?) {
        player.attachSurface(holder)
    }

    fun playSongAtIndex(index: Int) {
        if (index in _playlist.value.indices) {
            _currentIndex.value = index
            loadSong(index, autoPlay = true)
        }
    }

    private fun loadSong(index: Int, autoPlay: Boolean) {
        viewModelScope.launch {
            val song = _playlist.value.getOrNull(index) ?: return@launch
            // Fetch lyrics if missing
            val fullSong = if (song.parsedLyrics.isEmpty()) {
                val updated = repository.fetchLyricsForSong(song)
                val newList = _playlist.value.toMutableList()
                newList[index] = updated
                _playlist.value = newList
                updated
            } else song

            if (autoPlay) {
                player.playSong(
                    fullSong,
                    _settings.value.vocalSuppressionMode,
                    _settings.value.vocalSuppressionIntensity
                )
            }
        }
    }

    fun togglePlayPause() {
        if (isPrepared.value) {
            player.togglePlayPause()
        } else {
            val song = currentSong.value
            if (song != null) {
                player.playSong(
                    song,
                    _settings.value.vocalSuppressionMode,
                    _settings.value.vocalSuppressionIntensity
                )
            }
        }
    }

    fun stop() {
        player.stop()
    }

    fun playNext() {
        val list = _playlist.value
        if (list.isEmpty()) return
        val nextIndex = (_currentIndex.value + 1) % list.size
        playSongAtIndex(nextIndex)
    }

    fun playPrevious() {
        val list = _playlist.value
        if (list.isEmpty()) return
        val prevIndex = if (_currentIndex.value - 1 < 0) list.size - 1 else _currentIndex.value - 1
        playSongAtIndex(prevIndex)
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    // Vocal Suppression Settings
    fun setVocalSuppressionMode(mode: VocalSuppressionMode) {
        _settings.value = _settings.value.copy(vocalSuppressionMode = mode)
        player.applyVocalSuppression(mode, _settings.value.vocalSuppressionIntensity)
        val msg = when (mode) {
            VocalSuppressionMode.OFF -> "Modo normal: Voz original activada"
            VocalSuppressionMode.CENTER_CUT -> "Voz suprimida: Cancelación central (L-R)"
            VocalSuppressionMode.VOCAL_BAND_EQ -> "Voz suprimida: Filtro ecualizador karaoke"
            VocalSuppressionMode.MAX_KARAOKE -> "Karaoke Extremo: Máxima supresión de voz"
        }
        showMessage(msg)
    }

    fun setVocalSuppressionIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 1f)
        _settings.value = _settings.value.copy(vocalSuppressionIntensity = clamped)
        player.applyVocalSuppression(_settings.value.vocalSuppressionMode, clamped)
    }

    // AutoTune & Microphone Controls
    fun setAutoTuneLevel(level: Int) {
        val clamped = level.coerceIn(1, 10)
        _settings.value = _settings.value.copy(autoTuneLevel = clamped)
        if (isMicMonitoring.value) {
            restartMic()
        }
        showMessage("Autotune configurado en nivel $clamped/10")
        syncDspEngine()
    }

    fun toggleAutoTune(enabled: Boolean) {
        _settings.value = _settings.value.copy(isAutoTuneActive = enabled)
        syncDspEngine()
    }

    fun setMicGain(gain: Float) {
        val clamped = gain.coerceIn(0.1f, 2.5f)
        _settings.value = _settings.value.copy(micGain = clamped)
        syncDspEngine()
    }

    fun toggleMicMonitoring() {
        val shouldStart = !isMicMonitoring.value
        _settings.value = _settings.value.copy(isMicMonitoringActive = shouldStart)
        if (shouldStart) {
            restartMic()
            showMessage("Micrófono activado con Efectos y Autotune")
        } else {
            micEngine.stopListening()
            showMessage("Micrófono pausado")
        }
    }

    private fun syncDspEngine() {
        val s = _settings.value
        micEngine.updateDspSettings(
            eqConfig = s.micEqualizer,
            effectsConfig = s.voiceEffects,
            gain = s.micGain,
            autoTuneLevel = s.autoTuneLevel,
            isAutoTuneActive = s.isAutoTuneActive
        )
    }

    private fun restartMic() {
        val s = _settings.value
        micEngine.startListening(
            autoTuneLevel = s.autoTuneLevel,
            isAutoTuneActive = s.isAutoTuneActive,
            micGain = s.micGain,
            eqConfig = s.micEqualizer,
            effectsConfig = s.voiceEffects,
            enablePassThrough = true
        )
    }

    // Voice Effects
    fun setVoiceEffect(effect: VoiceEffectType) {
        val newFx = _settings.value.voiceEffects.copy(selectedEffect = effect)
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
        showMessage("Efecto de voz: ${effect.label}")
    }

    fun setVoiceEffectIntensity(intensity: Float) {
        val newFx = _settings.value.voiceEffects.copy(effectIntensity = intensity.coerceIn(0f, 1f))
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
    }

    fun toggleEcho(enabled: Boolean) {
        val newFx = _settings.value.voiceEffects.copy(isEchoEnabled = enabled)
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
        showMessage(if (enabled) "Eco activado" else "Eco desactivado")
    }

    fun setEchoIntensity(intensity: Float) {
        val newFx = _settings.value.voiceEffects.copy(echoIntensity = intensity.coerceIn(0f, 1f))
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
    }

    fun setEchoDelay(delayMs: Int) {
        val newFx = _settings.value.voiceEffects.copy(echoDelayMs = delayMs.coerceIn(100, 500))
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
    }

    fun toggleReverb(enabled: Boolean) {
        val newFx = _settings.value.voiceEffects.copy(isReverbEnabled = enabled)
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
        showMessage(if (enabled) "Reverberación activada" else "Reverberación desactivada")
    }

    fun setReverbIntensity(intensity: Float) {
        val newFx = _settings.value.voiceEffects.copy(reverbIntensity = intensity.coerceIn(0f, 1f))
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
    }

    fun setReverbRoomSize(roomSize: Float) {
        val newFx = _settings.value.voiceEffects.copy(reverbRoomSize = roomSize.coerceIn(0.1f, 1.0f))
        _settings.value = _settings.value.copy(voiceEffects = newFx)
        syncDspEngine()
    }

    // Graphic Equalizer
    fun toggleEqualizer(enabled: Boolean) {
        val newEq = _settings.value.micEqualizer.copy(isEnabled = enabled)
        _settings.value = _settings.value.copy(micEqualizer = newEq)
        syncDspEngine()
        showMessage(if (enabled) "Ecualizador activado" else "Ecualizador en bypass")
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        val currentGains = _settings.value.micEqualizer.bandGains.toMutableList()
        if (bandIndex in currentGains.indices) {
            currentGains[bandIndex] = gainDb.coerceIn(-12f, 12f)
            val newEq = _settings.value.micEqualizer.copy(bandGains = currentGains)
            _settings.value = _settings.value.copy(micEqualizer = newEq)
            syncDspEngine()
        }
    }

    fun setEqPreset(preset: EqPreset) {
        val newEq = _settings.value.micEqualizer.copy(
            bandGains = preset.gains,
            selectedPreset = preset,
            isEnabled = true
        )
        _settings.value = _settings.value.copy(micEqualizer = newEq)
        syncDspEngine()
        showMessage("Preset de EQ: ${preset.label}")
    }

    fun resetEqualizer() {
        setEqPreset(EqPreset.FLAT)
    }

    fun refreshMicDevices() {
        micEngine.updateAudioDevices()
    }

    // Live Recording
    fun toggleLiveRecording() {
        if (isRecording.value) {
            val item = recorder.stopRecording()
            if (item != null) {
                _recordings.value = repository.getRecordings()
                showMessage("¡Grabación guardada con éxito!")
            } else {
                showMessage("Grabación cancelada o vacía")
            }
        } else {
            val songName = currentSong.value?.title ?: "Karaoke"
            val started = recorder.startRecording(songName)
            if (started) {
                showMessage("Grabando tu voz en vivo... 🎙️")
                // Also ensure mic engine is active
                if (!isMicMonitoring.value) {
                    toggleMicMonitoring()
                }
            } else {
                showMessage("No se pudo iniciar la grabación (requiere permiso de micrófono)")
            }
        }
    }

    fun deleteRecording(item: RecordingItem) {
        repository.deleteRecording(item)
        _recordings.value = repository.getRecordings()
        showMessage("Grabación eliminada")
    }

    // Visual Customizations
    fun setActiveLyricColor(color: Color) {
        _settings.value = _settings.value.copy(activeLyricColor = color)
    }

    fun setInactiveLyricColor(color: Color) {
        _settings.value = _settings.value.copy(inactiveLyricColor = color)
    }

    fun setLyricSize(size: LyricSize) {
        _settings.value = _settings.value.copy(lyricSize = size)
    }

    fun setStageTheme(theme: StageTheme) {
        _settings.value = _settings.value.copy(selectedTheme = theme)
    }

    fun toggleShowVideo(show: Boolean) {
        _settings.value = _settings.value.copy(showVideoIfAvailable = show)
    }

    // Search and Playlist Multi-Select
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce while user is typing
            _isSearching.value = true
            val results = when (_searchSource.value) {
                SearchSource.ALL -> {
                    val local = repository.scanDeviceAudioFiles(50).filter {
                        it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
                    }
                    val net = repository.searchInternetSongs(query)
                    (local + net).distinctBy { "${it.title}_${it.artist}" }
                }
                SearchSource.LOCAL -> {
                    repository.scanDeviceAudioFiles(50).filter {
                        it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
                    }
                }
                SearchSource.INTERNET -> {
                    repository.searchInternetSongs(query)
                }
            }
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun setSearchSource(source: SearchSource) {
        _searchSource.value = source
        onSearchQueryChanged(_searchQuery.value)
    }

    fun toggleSongSelection(songId: String) {
        val current = _selectedSearchSongIds.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        _selectedSearchSongIds.value = current
    }

    fun addSelectedToPlaylist() {
        val selectedIds = _selectedSearchSongIds.value
        if (selectedIds.isEmpty()) return
        val songsToAdd = _searchResults.value.filter { it.id in selectedIds }
        val currentList = _playlist.value.toMutableList()
        currentList.addAll(songsToAdd)
        _playlist.value = currentList.distinctBy { it.id }
        _selectedSearchSongIds.value = emptySet()
        showMessage("Se agregaron ${songsToAdd.size} canciones a la playlist")
    }

    fun addSongToPlaylist(song: KaraokeSong) {
        val currentList = _playlist.value.toMutableList()
        if (currentList.none { it.id == song.id }) {
            currentList.add(song)
            _playlist.value = currentList
            showMessage("'${song.title}' agregada a la playlist")
        } else {
            showMessage("La canción ya está en la playlist")
        }
    }

    fun removeSongFromPlaylist(index: Int) {
        val currentList = _playlist.value.toMutableList()
        if (index in currentList.indices) {
            val removed = currentList.removeAt(index)
            _playlist.value = currentList
            if (_currentIndex.value >= currentList.size) {
                _currentIndex.value = (currentList.size - 1).coerceAtLeast(0)
            }
            showMessage("Eliminada: ${removed.title}")
        }
    }

    fun importLocalFile(uri: Uri) {
        viewModelScope.launch {
            showMessage("Analizando archivo y buscando letra...")
            val importedSong = repository.loadSongFromUri(uri)
            val currentList = _playlist.value.toMutableList()
            currentList.add(0, importedSong)
            _playlist.value = currentList
            playSongAtIndex(0)
            showMessage("¡Canción cargada! '${importedSong.title}' lista para cantar")
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        micEngine.stopListening()
        recorder.release()
    }
}
