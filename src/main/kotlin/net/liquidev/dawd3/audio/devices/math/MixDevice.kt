package net.liquidev.dawd3.audio.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier

class MixDevice : Device<MixDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "mix")

        val aAmplitudeControl = ControlDescriptor(id, "a_amplitude", 1f)
        val bAmplitudeControl = ControlDescriptor(id, "b_amplitude", 1f)

        val aPort = InputPortName(id, "a")
        val bPort = InputPortName(id, "b")
        val outputPort = OutputPortName(id, "output")
    }

    class Controls : ControlSet {
        val aAmplitude = FloatControl(aAmplitudeControl)
        val bAmplitude = FloatControl(bAmplitudeControl)

        override fun visitControls(visit: (ControlName, Control) -> Unit) {
            visit(aAmplitudeControl.name, aAmplitude)
            visit(bAmplitudeControl.name, bAmplitude)
        }
    }

    val a = InputPort()
    val b = InputPort()
    val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, controls: Controls) {
        val aAmplitude = controls.aAmplitude.value
        val bAmplitude = controls.bAmplitude.value

        val aBuffer = a.getConnectedOutputBuffer(0, sampleCount)
        val bBuffer = b.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)

        for (i in 0 until sampleCount) {
            outputBuffer[i] = aBuffer[i] * aAmplitude + bBuffer[i] * bAmplitude
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(aPort, a)
        visit(bPort, b)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}