package net.liquidev.dawd3.sound

import net.liquidev.d3r.AudioOutputStream

abstract class AudioGenerator : AudioOutputStream {
    private var outputBuffer: FloatArray? = null

    private fun allocateOutputBuffer(sampleCount: Int): FloatArray {
        val inOutputBuffer = outputBuffer
        if (inOutputBuffer == null || inOutputBuffer.size < sampleCount) {
            outputBuffer = FloatArray(sampleCount)
        }
        return outputBuffer!!
    }

    abstract fun generate(output: FloatArray, sampleCount: Int, channels: Int)

    override fun getOutputBuffer(sampleCount: Int, channels: Int): FloatArray {
        val outputBuffer = allocateOutputBuffer(sampleCount)
        generate(outputBuffer, sampleCount, channels)
        return outputBuffer
    }

    override fun error(message: String) {
        println("Error reported by audio stream: $message")
    }
}