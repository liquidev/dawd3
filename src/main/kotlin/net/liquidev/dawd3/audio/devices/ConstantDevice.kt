package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.audio.device.Device
import net.liquidev.dawd3.audio.device.OutputPort
import net.liquidev.dawd3.audio.device.PortName

class ConstantDevice(var value: Float) : Device {
    object Output : PortName()

    val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, channels: Int) {
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            outputBuffer[i] = value
        }
    }

    override fun visitOutputPorts(visit: (PortName, OutputPort) -> Unit) {
        visit(Output, output)
    }
}