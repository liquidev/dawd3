package net.liquidev.dawd3.audio.generator

import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.device.Sim
import net.liquidev.dawd3.audio.devices.TerminalDevice

/** Audio generator that evaluates a device graph. */
class DeviceGraphGenerator : AudioGenerator() {
    private val sim = Sim()
    private val terminalDeviceState = TerminalDevice()
    val terminalDevice = DeviceInstance(terminalDeviceState)

    /**
     * This should be called whenever the device graph changes to reestablish what order devices
     * should be processed in.
     */
    fun rebuildSim() {
        sim.rebuild(terminalDevice)
    }

    override fun generate(output: FloatArray, sampleCount: Int, channelCount: Int) {
        sim.simulate(sampleCount, channelCount)
        val buffer = terminalDeviceState.input.getConnectedOutputBuffer(0, sampleCount)
        for (i in 0 until sampleCount) {
            output[i] = buffer[i]
        }
    }
}