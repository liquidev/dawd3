package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.Device
import net.liquidev.dawd3.audio.device.DeviceDescriptor
import net.liquidev.dawd3.audio.device.InputPort
import net.liquidev.dawd3.audio.device.InputPortName
import net.minecraft.util.Identifier

class TerminalDevice : Device {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "terminal")
        val inputPort = InputPortName(id, "input")
    }

    val input = InputPort()

    override fun process(sampleCount: Int, channels: Int) {
        // Terminals don't do any audio processing.
        // The output port connected to `input` is instead used by terminal block entities like
        // speakers.
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(inputPort, input)
    }
}