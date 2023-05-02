package net.liquidev.dawd3.audio.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier

class FmaDevice : Device<FmaDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "fma")

        val inputPort = InputPortName(id, "input")
        val outputPort = OutputPortName(id, "output")

        val multiplyControl = ControlDescriptor(id, "multiply", 1f)
        val addControl = ControlDescriptor(id, "add", 0f)
    }

    class Controls : ControlSet {
        val multiply = FloatControl(multiplyControl)
        val add = FloatControl(addControl)

        override fun visitControls(visit: (ControlName, Control) -> Unit) {
            visit(multiplyControl.name, multiply)
            visit(addControl.name, add)
        }
    }

    val input = InputPort()
    val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, controls: Controls) {
        val inputBuffer = input.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        val multiply = controls.multiply.value
        val add = controls.add.value
        for (i in 0 until sampleCount) {
            outputBuffer[i] = inputBuffer[i] * multiply + add
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(inputPort, input)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}