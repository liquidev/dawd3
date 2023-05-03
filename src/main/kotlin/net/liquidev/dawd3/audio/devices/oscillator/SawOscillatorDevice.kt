package net.liquidev.dawd3.audio.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.audio.math.PhaseDerivative
import net.liquidev.dawd3.audio.math.oversample
import net.liquidev.dawd3.audio.math.polyBLEP
import net.minecraft.util.Identifier

class SawOscillatorDevice : Device<NoControls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "saw_oscillator")
        val phasePort = InputPortName(id, "phase")
        val outputPort = OutputPortName(id, "output")

        // TODO: Make this into a couple controls and put it into a nice UI
        const val usePolyBLEP = true
        const val oversampling = 4
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
            outputBuffer[i] = oversample(phase, deltaPhase, oversampling, ::saw)
        }
    }

    private fun saw(t: Float, dt: Float): Float {
        val naiveSaw = t * 2f - 1f
        return if (usePolyBLEP) {
            naiveSaw - polyBLEP(t, dt)
        } else {
            naiveSaw
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(phasePort, phase)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}