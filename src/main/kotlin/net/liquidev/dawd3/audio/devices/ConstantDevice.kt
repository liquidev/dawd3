package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.Device
import net.liquidev.dawd3.audio.device.DeviceDescriptor
import net.liquidev.dawd3.audio.device.OutputPort
import net.liquidev.dawd3.audio.device.OutputPortName
import net.minecraft.util.Identifier

class ConstantDevice(var value: Float) : Device {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "constant")
        val outputPort = OutputPortName(id, "output")
    }

    val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, channels: Int) {
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            outputBuffer[i] = value
        }
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}