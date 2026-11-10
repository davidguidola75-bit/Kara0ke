package com.example

import com.example.audio.VoiceDspProcessor
import com.example.data.model.EqPreset
import com.example.data.model.MicEqualizerConfig
import com.example.data.model.VoiceEffectType
import com.example.data.model.VoiceEffectsConfig
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceDspProcessorTest {

    @Test
    fun processBuffer_equalizer_processesWithoutDistortionOrCrash() {
        val dsp = VoiceDspProcessor(44100)
        val eqConfig = MicEqualizerConfig(
            isEnabled = true,
            bandGains = EqPreset.VOCAL_CLARITY.gains
        )
        val fxConfig = VoiceEffectsConfig()

        val input = ShortArray(512) { (it % 1000).toShort() }
        val output = ShortArray(512)

        dsp.processBuffer(input, output, 512, eqConfig, fxConfig)

        for (i in 0 until 512) {
            assertTrue(output[i] in -32768..32767)
        }
    }

    @Test
    fun processBuffer_echoAndReverb_runsSmoothly() {
        val dsp = VoiceDspProcessor(44100)
        val eqConfig = MicEqualizerConfig(isEnabled = false)
        val fxConfig = VoiceEffectsConfig(
            isEchoEnabled = true,
            echoIntensity = 0.5f,
            echoDelayMs = 200,
            isReverbEnabled = true,
            reverbIntensity = 0.4f
        )

        val input = ShortArray(1024) { 1500 }
        val output = ShortArray(1024)

        dsp.processBuffer(input, output, 1024, eqConfig, fxConfig)
        assertNotNull(output)
        assertTrue(output.any { it != 0.toShort() })
    }

    @Test
    fun processBuffer_voiceChangers_processAllModes() {
        val dsp = VoiceDspProcessor(44100)
        val eqConfig = MicEqualizerConfig(isEnabled = false)

        val modes = listOf(
            VoiceEffectType.CHIPMUNK,
            VoiceEffectType.MONSTER,
            VoiceEffectType.ROBOT,
            VoiceEffectType.MEGAPHONE,
            VoiceEffectType.ALIEN
        )

        for (mode in modes) {
            val fxConfig = VoiceEffectsConfig(
                selectedEffect = mode,
                effectIntensity = 0.8f
            )
            val input = ShortArray(256) { 3000 }
            val output = ShortArray(256)

            dsp.processBuffer(input, output, 256, eqConfig, fxConfig)
            assertTrue(output.any { it != 0.toShort() })
        }
    }
}
