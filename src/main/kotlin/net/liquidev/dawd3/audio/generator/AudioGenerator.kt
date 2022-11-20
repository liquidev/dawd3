package net.liquidev.dawd3.audio.generator

import net.liquidev.d3r.AudioOutputStream
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.AudioBuffer

abstract class AudioGenerator : AudioOutputStream {
    private companion object {
        val logger = Mod.logger<AudioGenerator>()
    }

    private var outputBuffer = AudioBuffer()

    abstract fun generate(output: FloatArray, sampleCount: Int, channelCount: Int)

    override fun getOutputBuffer(sampleCount: Int, channelCount: Int): FloatArray {
        val outputArray = outputBuffer.getOrReallocate(sampleCount)
        try {
            generate(outputArray, sampleCount, channelCount)
        } catch (e: Exception) {
            logger.error("exception occured in audio generator")
            e.printStackTrace()
        }
        return outputArray
    }

    override fun error(message: String) {
        println("Error reported by audio stream: $message")
    }
}