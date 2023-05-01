package net.liquidev.dawd3.audio.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier
import kotlin.math.abs

class TriangleOscillatorDevice : Device<NoControls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "triangle_oscillator")
        val phasePort = InputPortName(id, "phase")
        val outputPort = OutputPortName(id, "output")

        // TODO: Make this into a couple controls and put it into a nice UI
        const val usePolyBLEP = false
        const val oversampling = 1
    }

    private val phase = InputPort()
    private val output = OutputPort(bufferCount = 1)

    private val phaseDerivative = PhaseDerivative()
    private var previous = 0f

    override fun process(sampleCount: Int, controls: NoControls) {
        val phaseBuffer = phase.getConnectedOutputBuffer(0, sampleCount)

        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            val phase = phaseBuffer[i]
            val deltaPhase = phaseDerivative.stepNextDerivative(phase)
            outputBuffer[i] = oversample(phase, deltaPhase, oversampling, ::triangle)
        }
    }

    private fun triangle(t: Float, dt: Float): Float {
        return if (usePolyBLEP) {
            val naivePulse = if (t > 0.5f) 1f else -1f
            val pulse = naivePulse + polyBLEP((t + 0.5f) % 1f, dt) - polyBLEP(t, dt)
            val triangle = dt * pulse + (1f - dt) * previous
            previous = triangle * 0.99f
            triangle * 4f
        } else {
            val saw = t * 2f - 1f
            2f * abs(saw) - 1f
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(phasePort, phase)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}