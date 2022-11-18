package net.liquidev.dawd3.sound

import kotlin.math.sin

class SineOscGenerator(frequency: Float, private val amplitude: Float) : AudioGenerator() {
    private val phaseStep = (1.0f / Sound.sampleRate.toFloat()) * frequency
    private var phase = 0.0f

    private fun synthesize(): Float {
        phase += phaseStep
        phase %= 1.0f
        return sin(phase * 2.0f * kotlin.math.PI.toFloat()) * amplitude
    }

    override fun generate(output: FloatArray, sampleCount: Int, channels: Int) {
        for (i in 0 until sampleCount) {
            val sample = synthesize()
            output[i] = sample
        }
    }
}