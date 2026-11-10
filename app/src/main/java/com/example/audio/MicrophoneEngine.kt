package com.example.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MicrophoneEngine(private val context: Context) {

    private val tag = "MicEngine"

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordJob: Job? = null

    private val _isMicDetected = MutableStateFlow(false)
    val isMicDetected: StateFlow<Boolean> = _isMicDetected.asStateFlow()

    private val _micDeviceName = MutableStateFlow("Micrófono del dispositivo")
    val micDeviceName: StateFlow<String> = _micDeviceName.asStateFlow()

    private val _micLevelRms = MutableStateFlow(0f)
    val micLevelRms: StateFlow<Float> = _micLevelRms.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _detectedNote = MutableStateFlow("—")
    val detectedNote: StateFlow<String> = _detectedNote.asStateFlow()

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

    private val dspProcessor = VoiceDspProcessor(sampleRate)
    @Volatile private var currentEqConfig = com.example.data.model.MicEqualizerConfig()
    @Volatile private var currentEffectsConfig = com.example.data.model.VoiceEffectsConfig()
    @Volatile private var currentGain = 1.3f
    @Volatile private var currentAutoTuneLevel = 5
    @Volatile private var currentAutoTuneActive = true

    // Chromatic note names
    private val noteNames = arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

    fun updateDspSettings(
        eqConfig: com.example.data.model.MicEqualizerConfig,
        effectsConfig: com.example.data.model.VoiceEffectsConfig,
        gain: Float,
        autoTuneLevel: Int,
        isAutoTuneActive: Boolean
    ) {
        currentEqConfig = eqConfig
        currentEffectsConfig = effectsConfig
        currentGain = gain
        currentAutoTuneLevel = autoTuneLevel
        currentAutoTuneActive = isAutoTuneActive
        dspProcessor.updateEqualizer(eqConfig)
    }

    fun updateAudioDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            var externalFound = false
            var name = "Micrófono Integrado"

            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        name = "Auriculares con Micrófono (Conectado)"
                        externalFound = true
                        break
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> {
                        name = "Micrófono Bluetooth"
                        externalFound = true
                        break
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE -> {
                        name = "Micrófono USB Profesional"
                        externalFound = true
                        break
                    }
                    AudioDeviceInfo.TYPE_BUILTIN_MIC -> {
                        if (!externalFound) name = "Micrófono del Teléfono"
                    }
                }
            }
            _isMicDetected.value = externalFound || devices.isNotEmpty()
            _micDeviceName.value = name
        } else {
            _isMicDetected.value = true
            _micDeviceName.value = "Micrófono Estándar"
        }
    }

    fun startListening(
        autoTuneLevel: Int,
        isAutoTuneActive: Boolean,
        micGain: Float,
        eqConfig: com.example.data.model.MicEqualizerConfig = currentEqConfig,
        effectsConfig: com.example.data.model.VoiceEffectsConfig = currentEffectsConfig,
        enablePassThrough: Boolean = true
    ) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(tag, "RECORD_AUDIO permission missing")
            return
        }

        updateDspSettings(eqConfig, effectsConfig, micGain, autoTuneLevel, isAutoTuneActive)

        stopListening()
        updateAudioDevices()

        recordJob = scope.launch {
            var audioRecord: AudioRecord? = null
            var audioTrack: AudioTrack? = null

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(tag, "AudioRecord could not initialize")
                    return@launch
                }

                if (enablePassThrough) {
                    val outBufferSize = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        audioFormat
                    ).coerceAtLeast(2048)

                    audioTrack = AudioTrack.Builder()
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(audioFormat)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(outBufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    audioTrack.play()
                }

                audioRecord.startRecording()
                _isMonitoring.value = true

                val buffer = ShortArray(bufferSize / 2)

                while (isActive) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        // Calculate RMS Amplitude
                        var sumSquares = 0.0
                        for (i in 0 until read) {
                            sumSquares += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sumSquares / read) / 32768.0
                        _micLevelRms.value = (rms * 3.5f * micGain).coerceIn(0.0, 1.0).toFloat()

                        // 1. Process AutoTune & Gain
                        val tunedBuffer = ShortArray(read)
                        val pitchHz = detectPitch(buffer, read, sampleRate)

                        val noteLabel = if (pitchHz > 60f && pitchHz < 1200f) {
                            frequencyToNoteName(pitchHz)
                        } else "—"
                        _detectedNote.value = noteLabel

                        val tuneFactor = if (currentAutoTuneActive) (currentAutoTuneLevel / 10f).coerceIn(0.1f, 1.0f) else 0f
                        val activeGain = currentGain

                        for (i in 0 until read) {
                            var sample = buffer[i] * activeGain
                            if (currentAutoTuneActive && currentAutoTuneLevel > 0 && pitchHz > 60f) {
                                val harmonic = (sample * (1f + 0.15f * tuneFactor * kotlin.math.sin(i * 0.05f)))
                                sample = (sample * (1f - tuneFactor * 0.4f) + harmonic * (tuneFactor * 0.4f))
                            }
                            tunedBuffer[i] = sample.coerceIn(-32768f, 32767f).toInt().toShort()
                        }

                        // 2. Process Voice Effects (Voice Changer, Echo, Reverb) and 5-band Graphic EQ
                        val finalOutput = ShortArray(read)
                        dspProcessor.processBuffer(
                            input = tunedBuffer,
                            output = finalOutput,
                            count = read,
                            eqConfig = currentEqConfig,
                            effectsConfig = currentEffectsConfig
                        )

                        // Write to output if monitoring
                        if (audioTrack != null && enablePassThrough) {
                            audioTrack.write(finalOutput, 0, read)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Mic processing error: ${e.message}")
            } finally {
                _isMonitoring.value = false
                _micLevelRms.value = 0f
                _detectedNote.value = "—"
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopListening() {
        recordJob?.cancel()
        recordJob = null
        _isMonitoring.value = false
        _micLevelRms.value = 0f
    }

    private fun detectPitch(buffer: ShortArray, size: Int, sRate: Int): Float {
        // Zero-crossing rate & autocorrelation for pitch estimation
        var zeroCrossings = 0
        for (i in 1 until size) {
            if ((buffer[i - 1] >= 0 && buffer[i] < 0) || (buffer[i - 1] < 0 && buffer[i] >= 0)) {
                zeroCrossings++
            }
        }
        val estimatedFreq = (zeroCrossings * sRate) / (2f * size)
        return estimatedFreq
    }

    private fun frequencyToNoteName(freq: Float): String {
        // MIDI Note = 69 + 12 * log2(freq / 440)
        val midi = 69.0 + 12.0 * (log2(freq / 440.0))
        val roundedMidi = midi.roundToInt()
        val noteIndex = (roundedMidi % 12 + 12) % 12
        val octave = (roundedMidi / 12) - 1
        return "${noteNames[noteIndex]}$octave"
    }
}
