package net.liquidev.dawd3.audio.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier
import kotlin.math.abs

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

    private var previousPhase = 0f
    private var previousDeltaPhase = 0f

    override fun process(sampleCount: Int, controls: NoControls) {
        val phaseBuffer = phase.getConnectedOutputBuffer(0, sampleCount)

        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            val phase = phaseBuffer[i]
            val deltaPhase = phase - previousPhase

            // Smooth out the delta phase at spikes, as then our PolyBLEP dt is no longer accurate.
            val smoothDeltaPhase = if (abs(deltaPhase) > 0.999) previousDeltaPhase else deltaPhase

            previousPhase = phase
            previousDeltaPhase = deltaPhase

            var accumulator = 0f
            for (sample in 0 until oversampling) {
                val t = sample.toFloat() / oversampling.toFloat()
                accumulator += saw(
                    phase + smoothDeltaPhase * t,
                    smoothDeltaPhase
                )
            }
            outputBuffer[i] = accumulator / oversampling
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

    private fun polyBLEP(t: Float, dt: Float) =
        if (t < dt) {
            val tt = t / dt
            tt + tt - tt * tt - 1f
        } else if (t > 1f - dt) {
            val tt = (t - 1f) / dt
            tt * tt + tt + tt + 1f
        } else {
            0f
        }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(phasePort, phase)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}