package net.liquidev.dawd3.audio.generator

import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.unit.Amplitude
import kotlin.math.sin

class SineOscGenerator(frequency: Float, private val amplitude: Amplitude) : AudioGenerator() {
    private val phaseStep = (1.0f / Audio.sampleRate.toFloat()) * frequency
    private var phase = 0.0f

    private fun synthesize(): Float {
        phase += phaseStep
        phase %= 1.0f
        return sin(phase * 2.0f * kotlin.math.PI.toFloat())
    }

    override fun generate(output: FloatArray, sampleCount: Int, channelCount: Int) {
        for (i in 0 until sampleCount) {
            val sample = synthesize()
            output[i] = sample
        }
    }
}