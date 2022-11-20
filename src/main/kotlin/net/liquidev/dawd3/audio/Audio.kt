package net.liquidev.dawd3.audio

import net.liquidev.d3r.D3r
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.generator.MixGenerator
import net.liquidev.dawd3.audio.unit.Decibels

/** Audio system and common settings. */
object Audio {
    val logger = Mod.logger<Audio>()

    const val sampleRate = 48000
    const val sampleRateF = sampleRate.toFloat()
    const val sampleRateFInv = 1.0f / sampleRateF
    private const val bufferSize = 256

    private val outputDeviceId: Int
    private val outputStreamId: Int

    val mixer = MixGenerator()

    init {
        logger.info("initializing")
        logger.info("${Decibels(-3.0f).toAmplitude()}")
        D3r.openDefaultHost()
        outputDeviceId = D3r.openDefaultOutputDevice()
        outputStreamId = D3r.openOutputStream(outputDeviceId, sampleRate, 1, bufferSize, mixer)
        D3r.startPlayback(outputStreamId)
    }

    fun forceInitializationNow() {}

    fun deinitialize() {
        D3r.closeOutputStream(outputStreamId)
        D3r.closeOutputDevice(outputDeviceId)
    }
}