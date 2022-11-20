package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.audio.device.Device
import net.liquidev.dawd3.audio.device.InputPort
import net.liquidev.dawd3.audio.device.PortName

class TerminalDevice : Device {
    object Input : PortName()

    val input = InputPort()

    override fun process(sampleCount: Int, channels: Int) {
        // Terminals don't do any audio processing.
        // The output port connected to `input` is instead used by terminal block entities like
        // speakers.
    }

    override fun visitInputPorts(visit: (PortName, InputPort) -> Unit) {
        visit(Input, input)
    }
}