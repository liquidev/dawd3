package net.liquidev.dawd3.audio

import net.liquidev.d3r.D3r
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.generator.GeneratorWithProcessingState
import net.liquidev.dawd3.audio.generator.MixGenerator
import net.liquidev.dawd3.audio.unit.Frequency

/** Audio system and common settings. */
object Audio {
    private val logger = Mod.logger<Audio>()

    const val sampleRate = 48000
    const val sampleRateF = sampleRate.toFloat()
    const val sampleRateFInv = 1.0f / sampleRateF
    private const val bufferSize = 256

    val a4 = Frequency(440f)

    private val outputDeviceId: Int
    private val outputStreamId: Int

    val mixer = MixGenerator()
    private val processingStateAdapter = GeneratorWithProcessingState(mixer)
    val processingState get() = processingStateAdapter.processingState

    init {
        logger.info("initializing")
        D3r.openDefaultHost()
        outputDeviceId = D3r.openDefaultOutputDevice()
        outputStreamId =
            D3r.openOutputStream(outputDeviceId, sampleRate, 1, bufferSize, processingStateAdapter)
        D3r.startPlayback(outputStreamId)
    }

    fun initializeClient() {
        // Stubbed out; this is called explicitly to force the otherwise lazy initialization
        // of Audio.
    }

    fun deinitialize() {
        D3r.closeOutputStream(outputStreamId)
        D3r.closeOutputDevice(outputDeviceId)
    }
}