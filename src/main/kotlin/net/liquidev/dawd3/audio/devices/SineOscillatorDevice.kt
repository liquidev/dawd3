package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier
import kotlin.math.sin

private const val twoPi = 2.0f * kotlin.math.PI.toFloat()

class SineOscillatorDevice : Device<NoControls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "sine_oscillator")
        val phasePort = InputPortName(id, "phase")
        val outputPort = OutputPortName(id, "output")
    }

    private val phase = InputPort()
    private val output = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, channels: Int, controls: NoControls) {
        val phase = phase.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            outputBuffer[i] = sin(phase[i] * twoPi)
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(phasePort, phase)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}