package com.example.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.RecordingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KaraokeRecorder(private val context: Context) {

    private val tag = "KaraokeRecorder"
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private var currentSongTitle: String = ""

    fun startRecording(songTitle: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(tag, "Cannot record: RECORD_AUDIO permission missing")
            return false
        }

        stopRecording()

        currentSongTitle = songTitle.ifBlank { "Mi Grabación" }
        val recordingsDir = File(context.filesDir, "karaoke_recordings")
        if (!recordingsDir.exists()) recordingsDir.mkdirs()

        val safeTitle = currentSongTitle.replace(Regex("[^a-zA-Z0-9_ -]"), "").take(30)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(recordingsDir, "Karaoke_${safeTitle}_$timeStamp.m4a")
        currentOutputFile = file

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(192000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _recordingSeconds.value = 0

            timerJob = scope.launch {
                while (isActive && _isRecording.value) {
                    delay(1000)
                    _recordingSeconds.value += 1
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start recording: ${e.message}")
            currentOutputFile?.delete()
            currentOutputFile = null
            _isRecording.value = false
            return false
        }
    }

    fun stopRecording(): RecordingItem? {
        timerJob?.cancel()
        timerJob = null

        val recorder = mediaRecorder
        val file = currentOutputFile
        val duration = _recordingSeconds.value

        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w(tag, "Error stopping recorder: ${e.message}")
        }
        mediaRecorder = null
        _isRecording.value = false
        _recordingSeconds.value = 0

        if (file != null && file.exists() && file.length() > 0) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return RecordingItem(
                id = file.name,
                songTitle = currentSongTitle,
                dateFormatted = dateFormat.format(Date()),
                durationSeconds = duration,
                filePath = file.absolutePath,
                fileSizeBytes = file.length()
            )
        }
        return null
    }

    fun release() {
        stopRecording()
    }
}
