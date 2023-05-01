package net.liquidev.dawd3.audio.generator

import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.device.NoControls
import net.liquidev.dawd3.audio.devices.TerminalDevice

/** Audio generator that evaluates a device graph. */
class DeviceGraphGenerator : AudioGenerator() {
    private val terminalDeviceState = TerminalDevice()
    val terminalDevice = DeviceInstance.create(terminalDeviceState, NoControls)

    override fun generate(output: FloatArray, sampleCount: Int, channelCount: Int) {
        // TODO: Maybe passing in the static processingState here is not the cleanest way to go
        //  about things, but I don't see how we could inject that context into this function
        //  without jumping through significant hoops.
        terminalDevice.process(sampleCount, channelCount, Audio.processingState)
        val buffer = terminalDeviceState.input.getConnectedOutputBuffer(0, sampleCount)
        for (i in 0 until sampleCount) {
            output[i] = buffer[i]
        }
    }
}