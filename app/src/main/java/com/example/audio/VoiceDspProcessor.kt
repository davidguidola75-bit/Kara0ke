package com.example.audio

import com.example.data.model.MicEqualizerConfig
import com.example.data.model.VoiceEffectType
import com.example.data.model.VoiceEffectsConfig
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class VoiceDspProcessor(private val sampleRate: Int = 44100) {

    // 5-band Graphic Equalizer Frequencies
    private val eqFrequencies = doubleArrayOf(60.0, 250.0, 1000.0, 4000.0, 12000.0)
    private val eqQ = doubleArrayOf(1.0, 1.2, 1.2, 1.2, 1.0)

    // Biquad coefficients per band: [b0, b1, b2, a1, a2]
    private val biquadCoeffs = Array(5) { DoubleArray(5) }
    // Biquad state per band: [x1, x2, y1, y2]
    private val biquadState = Array(5) { DoubleArray(4) }

    // Echo circular buffer (up to 1 second)
    private val echoBuffer = FloatArray(sampleRate)
    private var echoWriteIndex = 0

    // Reverb comb filters
    private val combDelays = intArrayOf(1116, 1188, 1277, 1356)
    private val combBuffers = Array(4) { FloatArray(1600) }
    private val combIndices = IntArray(4)

    // Voice Changer phase tracking
    private var robotPhase = 0.0
    private var alienPhase = 0.0
    private var pitchBuffer = FloatArray(2048)
    private var pitchWriteIndex = 0
    private var pitchReadPos = 0.0

    init {
        updateEqualizer(MicEqualizerConfig())
    }

    fun updateEqualizer(config: MicEqualizerConfig) {
        val gains = config.bandGains
        for (i in 0 until 5) {
            val gainDb = if (config.isEnabled && i in gains.indices) gains[i].toDouble() else 0.0
            computePeakingBiquad(i, eqFrequencies[i], eqQ[i], gainDb)
        }
    }

    private fun computePeakingBiquad(band: Int, f0: Double, q: Double, gainDb: Double) {
        if (abs(gainDb) < 0.05) {
            // Flat pass-through
            biquadCoeffs[band][0] = 1.0
            biquadCoeffs[band][1] = 0.0
            biquadCoeffs[band][2] = 0.0
            biquadCoeffs[band][3] = 0.0
            biquadCoeffs[band][4] = 0.0
            return
        }

        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * f0 / sampleRate
        val alpha = sin(w0) / (2.0 * q)
        val cosw0 = cos(w0)

        val b0 = 1.0 + alpha * a
        val b1 = -2.0 * cosw0
        val b2 = 1.0 - alpha * a
        val a0 = 1.0 + alpha / a
        val a1 = -2.0 * cosw0
        val a2 = 1.0 - alpha / a

        biquadCoeffs[band][0] = b0 / a0
        biquadCoeffs[band][1] = b1 / a0
        biquadCoeffs[band][2] = b2 / a0
        biquadCoeffs[band][3] = a1 / a0
        biquadCoeffs[band][4] = a2 / a0
    }

    fun processBuffer(
        input: ShortArray,
        output: ShortArray,
        count: Int,
        eqConfig: MicEqualizerConfig,
        effectsConfig: VoiceEffectsConfig
    ) {
        val hasVoiceEffect = effectsConfig.selectedEffect != VoiceEffectType.NONE
        val hasEcho = effectsConfig.isEchoEnabled && effectsConfig.echoIntensity > 0.02f
        val hasReverb = effectsConfig.isReverbEnabled && effectsConfig.reverbIntensity > 0.02f
        val hasEq = eqConfig.isEnabled

        // Echo parameters
        val echoDelaySamples = ((effectsConfig.echoDelayMs / 1000.0) * sampleRate).toInt().coerceIn(100, sampleRate - 1)
        val echoWet = (effectsConfig.echoIntensity * 0.75f).coerceIn(0f, 0.85f)
        val echoFeedback = 0.45f

        // Reverb parameters
        val reverbWet = (effectsConfig.reverbIntensity * 0.65f).coerceIn(0f, 0.75f)
        val reverbRoom = (effectsConfig.reverbRoomSize * 0.82f).coerceIn(0.2f, 0.92f)

        for (i in 0 until count) {
            var sample = input[i].toFloat()

            // 1. Voice Changer (Modulation / Pitch)
            if (hasVoiceEffect) {
                sample = applyVoiceEffect(sample, effectsConfig.selectedEffect, effectsConfig.effectIntensity)
            }

            // 2. Graphic Equalizer (5 Biquad filters in series)
            if (hasEq) {
                var eqSample = sample.toDouble()
                for (b in 0 until 5) {
                    val coeffs = biquadCoeffs[b]
                    val state = biquadState[b]
                    val y = coeffs[0] * eqSample + coeffs[1] * state[0] + coeffs[2] * state[1] - coeffs[3] * state[2] - coeffs[4] * state[3]
                    state[1] = state[0]
                    state[0] = eqSample
                    state[3] = state[2]
                    state[2] = y
                    eqSample = y
                }
                sample = eqSample.toFloat()
            }

            // 3. Reverb Effect
            if (hasReverb) {
                var reverbSum = 0f
                for (c in 0 until 4) {
                    val delay = (combDelays[c] * reverbRoom).toInt().coerceIn(100, combBuffers[c].size - 1)
                    var idx = combIndices[c]
                    val delayed = combBuffers[c][idx]
                    combBuffers[c][idx] = sample + delayed * reverbRoom * 0.7f
                    idx = (idx + 1) % delay
                    combIndices[c] = idx
                    reverbSum += delayed
                }
                sample = sample * (1f - reverbWet) + (reverbSum * 0.25f) * reverbWet
            }

            // 4. Echo Effect
            if (hasEcho) {
                var readIdx = echoWriteIndex - echoDelaySamples
                if (readIdx < 0) readIdx += sampleRate
                val delayedSample = echoBuffer[readIdx]

                echoBuffer[echoWriteIndex] = sample + delayedSample * echoFeedback
                echoWriteIndex = (echoWriteIndex + 1) % sampleRate

                sample = sample * (1f - echoWet * 0.5f) + delayedSample * echoWet
            }

            // Clamp and write to output
            output[i] = sample.coerceIn(-32768f, 32767f).toInt().toShort()
        }
    }

    private fun applyVoiceEffect(sample: Float, effect: VoiceEffectType, intensity: Float): Float {
        val dry = sample
        val wet: Float

        when (effect) {
            VoiceEffectType.NONE -> return dry

            VoiceEffectType.CHIPMUNK -> {
                // Pitch Up Resampler (1.5x)
                pitchBuffer[pitchWriteIndex] = sample
                pitchWriteIndex = (pitchWriteIndex + 1) % pitchBuffer.size

                pitchReadPos += 1.5
                if (pitchReadPos >= pitchBuffer.size) pitchReadPos -= pitchBuffer.size

                val readIdx = pitchReadPos.toInt() % pitchBuffer.size
                wet = pitchBuffer[readIdx] * 1.1f
            }

            VoiceEffectType.MONSTER -> {
                // Pitch Down Resampler (0.72x) + bass reinforcement
                pitchBuffer[pitchWriteIndex] = sample
                pitchWriteIndex = (pitchWriteIndex + 1) % pitchBuffer.size

                pitchReadPos += 0.72
                if (pitchReadPos >= pitchBuffer.size) pitchReadPos -= pitchBuffer.size

                val readIdx = pitchReadPos.toInt() % pitchBuffer.size
                val subOctave = pitchBuffer[(readIdx / 2) % pitchBuffer.size]
                wet = pitchBuffer[readIdx] * 0.7f + subOctave * 0.5f
            }

            VoiceEffectType.ROBOT -> {
                // Ring Modulation with 65Hz carrier wave
                robotPhase += 2.0 * PI * 65.0 / sampleRate
                if (robotPhase > 2.0 * PI) robotPhase -= 2.0 * PI
                val carrier = sin(robotPhase).toFloat()
                wet = sample * (carrier * 0.85f + 0.15f)
            }

            VoiceEffectType.MEGAPHONE -> {
                // Band-limited saturation
                val normalized = sample / 12000f
                val clipped = (normalized - (normalized * normalized * normalized) / 3f) * 12000f
                wet = clipped * 1.2f
            }

            VoiceEffectType.ALIEN -> {
                // Dual-tone phase modulation
                alienPhase += 2.0 * PI * 8.0 / sampleRate
                if (alienPhase > 2.0 * PI) alienPhase -= 2.0 * PI
                val lfo = (sin(alienPhase) * 120.0 + 160.0)
                robotPhase += 2.0 * PI * lfo / sampleRate
                if (robotPhase > 2.0 * PI) robotPhase -= 2.0 * PI
                wet = (sample * sin(robotPhase).toFloat()) * 1.15f
            }
        }

        return dry * (1f - intensity) + wet * intensity
    }
}
