package net.liquidev.dawd3.audio.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.audio.math.PhaseDerivative
import net.liquidev.dawd3.audio.math.oversample
import net.liquidev.dawd3.audio.math.polyBLEP
import net.minecraft.util.Identifier

class PulseOscillatorDevice : Device<PulseOscillatorDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "pulse_oscillator")

        val dutyCycleControl = ControlDescriptor(id, "duty_cycle", 0.5f)
        val dutyCycleCVControl = ControlDescriptor(id, "duty_cycle_cv", 0.5f)

        val phasePort = InputPortName(id, "phase")
        val dutyCyclePort = InputPortName(id, "duty_cycle")
        val outputPort = OutputPortName(id, "output")

        // TODO: Make this into a couple controls and put it into a nice UI
        const val usePolyBLEP = true
        const val oversampling = 4
    }

    class Controls : ControlSet {
        val dutyCycle = FloatControl(dutyCycleControl)
        val dutyCycleCV = FloatControl(dutyCycleCVControl)

        override fun visitControls(visit: (ControlName, Control) -> Unit) {
            visit(dutyCycleControl.name, dutyCycle)
            visit(dutyCycleCVControl.name, dutyCycleCV)
        }
    }

    private val phase = InputPort()
    private val dutyCycle = InputPort()
    private val output = OutputPort(bufferCount = 1)

    private val phaseDerivative = PhaseDerivative()

    override fun process(sampleCount: Int, controls: Controls) {
        val phaseBuffer = phase.getConnectedOutputBuffer(0, sampleCount)
        val dutyCycleBuffer = dutyCycle.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)

        val constantDutyCycle = controls.dutyCycle.value
        val dutyCycleCV = controls.dutyCycleCV.value

        for (i in 0 until sampleCount) {
            val phase = phaseBuffer[i]
            val deltaPhase = phaseDerivative.stepNextDerivative(phase)
            val dutyCycle = constantDutyCycle + dutyCycleBuffer[i] * dutyCycleCV
            outputBuffer[i] =
                oversample(phase, deltaPhase, oversampling) { t, dt -> pulse(t, dt, dutyCycle) }
        }
    }

    private fun pulse(t: Float, dt: Float, dutyCycle: Float): Float {
        val naivePulse = if (t > 1f - dutyCycle) 1f else -1f
        return if (usePolyBLEP) {
            naivePulse + polyBLEP((t + dutyCycle) % 1f, dt) - polyBLEP(t, dt)
        } else {
            naivePulse
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(phasePort, phase)
        visit(dutyCyclePort, dutyCycle)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}