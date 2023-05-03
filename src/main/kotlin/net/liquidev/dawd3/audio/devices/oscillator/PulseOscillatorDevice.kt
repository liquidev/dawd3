package net.liquidev.dawd3.audio.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.audio.math.PhaseDerivative
import net.liquidev.dawd3.audio.math.oversample
import net.liquidev.dawd3.audio.math.polyBLEP
import net.minecraft.util.Identifier

class PulseOscillatorDevice : Device<NoControls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "pulse_oscillator")
        val phasePort = InputPortName(id, "phase")
        val outputPort = OutputPortName(id, "output")

        // TODO: Make this into a couple controls and put it into a nice UI
        const val usePolyBLEP = true
        const val oversampling = 4

        // Especially this should be a knob+CV controlled thing.
        const val dutyCycle = 0.5f
    }

    private val phase = InputPort()
    private val output = OutputPort(bufferCount = 1)

    private val phaseDerivative = PhaseDerivative()

    override fun process(sampleCount: Int, controls: NoControls) {
        val phaseBuffer = phase.getConnectedOutputBuffer(0, sampleCount)

        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            val phase = phaseBuffer[i]
            val deltaPhase = phaseDerivative.stepNextDerivative(phase)
            outputBuffer[i] = oversample(phase, deltaPhase, oversampling, ::pulse)
        }
    }

    private fun pulse(t: Float, dt: Float): Float {
        val naivePulse = if (t > 1f - dutyCycle) 1f else -1f
        return if (usePolyBLEP) {
            naivePulse + polyBLEP((t + dutyCycle) % 1f, dt) - polyBLEP(t, dt)
        } else {
            naivePulse
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(phasePort, phase)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}