package net.liquidev.dawd3.audio.generator

import java.util.concurrent.atomic.AtomicBoolean

class PausableGenerator(val inner: AudioGenerator) : AudioGenerator() {
    private val internalPlaying = AtomicBoolean(true)
    var playing: Boolean
        get() = internalPlaying.get()
        set(value) = internalPlaying.set(value)

    override fun generate(output: FloatArray, sampleCount: Int, channelCount: Int) {
        if (playing) {
            inner.generate(output, sampleCount, channelCount)
        } else {
            for (i in 0 until (sampleCount * channelCount)) {
                output[i] = 0f
            }
        }
    }
}