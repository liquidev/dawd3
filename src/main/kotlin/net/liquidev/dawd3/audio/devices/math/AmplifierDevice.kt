package net.liquidev.dawd3.audio.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.common.lerp
import net.minecraft.util.Identifier

class AmplifierDevice : Device<AmplifierDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "amplifier")

        val amplitudeControl = ControlDescriptor(id, "amplitude", 1f)
        val amplitudeCVControl = ControlDescriptor(id, "amplitude_cv", 0f)

        val amplitudeCVPort = InputPortName(id, "amplitude_cv")
        val inputPort = InputPortName(id, "input")
        val outputPort = OutputPortName(id, "output")
    }

    class Controls : ControlSet {
        val amplitude = Control(amplitudeControl)
        val amplitudeCV = Control(amplitudeCVControl)

        override fun visitControls(visit: (ControlDescriptor, Control) -> Unit) {
            visit(amplitudeControl, amplitude)
            visit(amplitudeCVControl, amplitudeCV)
        }
    }

    val amplitudeCV = InputPort()
    val input = InputPort()
    val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, controls: Controls) {
        val constantAmplitude = controls.amplitude.value
        val amplitudeCVAmount = controls.amplitudeCV.value

        val amplitudeCVBuffer = amplitudeCV.getConnectedOutputBuffer(0, sampleCount)
        val inputBuffer = input.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)

        for (i in 0 until sampleCount) {
            val cvAmplitude = lerp(1f, amplitudeCVBuffer[i], amplitudeCVAmount)
            outputBuffer[i] = inputBuffer[i] * constantAmplitude * cvAmplitude
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(amplitudeCVPort, amplitudeCV)
        visit(inputPort, input)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}