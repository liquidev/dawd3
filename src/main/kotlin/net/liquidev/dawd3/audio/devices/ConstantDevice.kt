package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier

class ConstantDevice : Device<ConstantDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "constant")
        val valueControl = ControlDescriptor(id, "value", 0f)
        val outputPort = OutputPortName(id, "output")
    }

    class Controls : ControlSet {
        val value = Control(valueControl)

        override fun visitControls(visit: (ControlDescriptor, Control) -> Unit) {
            visit(valueControl, value)
        }
    }

    val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, channels: Int, controls: Controls) {
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            outputBuffer[i] = controls.value.value
        }
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}