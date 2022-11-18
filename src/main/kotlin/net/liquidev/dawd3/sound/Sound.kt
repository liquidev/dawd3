package net.liquidev.dawd3.sound

import net.liquidev.d3r.D3r

/** Common sound utilities. */
object Sound {
    const val sampleRate = 48000
    private const val bufferSize = 256

    val outputDeviceId: Int
    val outputStreamId: Int

    init {
        D3r.openDefaultHost()
        outputDeviceId = D3r.openDefaultOutputDevice()
        outputStreamId = D3r.openOutputStream(
            outputDeviceId,
            sampleRate,
            1,
            bufferSize,
            SineOscGenerator(frequency = 440.0f, amplitude = 0.5f)
        )
        D3r.startPlayback(outputStreamId)
    }

    fun forceInitializationNow() {}

    fun deinitialize() {
        D3r.closeOutputStream(outputStreamId)
        D3r.closeOutputDevice(outputDeviceId)
    }
}