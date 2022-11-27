package net.liquidev.dawd3.audio.generator

import net.liquidev.dawd3.audio.device.ProcessingState

/**
 * Adapter for adding device ProcessingState into an existing generator.
 * The processing state is reset with every call to generate().
 */
class GeneratorWithProcessingState(val inner: AudioGenerator) : AudioGenerator() {
    val processingState = ProcessingState()

    override fun generate(output: FloatArray, sampleCount: Int, channelCount: Int) {
        inner.generate(output, sampleCount, channelCount)
        processingState.reset()
    }
}