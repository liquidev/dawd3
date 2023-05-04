package net.liquidev.dawd3.audio.devices.filter

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.common.clamp
import net.minecraft.util.Identifier
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class BiquadDevice(val type: Type) : Device<BiquadDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "biquad")

        val inputPort = InputPortName(id, "input")
        val frequencyCVPort = InputPortName(id, "frequency_cv")
        val resonanceCVPort = InputPortName(id, "resonance_cv")
        val outputPort = OutputPortName(id, "output")

        val frequencyControl = ControlDescriptor(id, "frequency", 8000f)
        val frequencyCVControl = ControlDescriptor(id, "frequency_cv", 16000f)
        val resonanceControl = ControlDescriptor(id, "resonance", 0.5f)
        val resonanceCVControl = ControlDescriptor(id, "resonance_cv", 0.5f)
    }

    class Controls : ControlSet {
        val frequency = FloatControl(frequencyControl)
        val frequencyCV = FloatControl(frequencyCVControl)
        val resonance = FloatControl(resonanceControl)
        val resonanceCV = FloatControl(resonanceCVControl)

        override fun visitControls(visit: (ControlName, Control) -> Unit) {
            visit(frequencyControl.name, frequency)
            visit(frequencyCVControl.name, frequencyCV)
            visit(resonanceControl.name, resonance)
            visit(resonanceCVControl.name, resonanceCV)
        }
    }

    enum class Type {
        LowPass,
        HighPass,
        BandPass,
        Notch,
    }

    val input = InputPort()
    val frequencyCV = InputPort()
    val resonanceCV = InputPort()
    val output = OutputPort(bufferCount = 1)

    private var firstSample = true
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    private var b0a0 = 0f
    private var b1a0 = 0f
    private var b2a0 = 0f
    private var a1a0 = 0f
    private var a2a0 = 0f

    private var frequency: Float? = null
    private var resonance: Float? = null

    private fun updateCoefficients(frequency: Float, resonance: Float) {
        if (frequency == this.frequency && resonance == this.resonance) {
            return
        }

        this.frequency = frequency
        this.resonance = resonance

        // Need to clamp the frequency to 1Hz here because if it reaches 0 we get division by 0,
        // which explodes the entire state. Similar thing with resonance.
        val f = max(1f, frequency) * Audio.sampleRateFInv
        val r = clamp(resonance, 0f, 1f)

        val alpha = sin(f) * (1f - r)
        val cosW0 = cos(f)

        val a0 = 1f + alpha
        val a1 = -2f * cosW0
        val a2 = 1f - alpha
        var b0 = 0f
        var b1 = 0f
        var b2 = 0f

        when (type) {
            Type.LowPass -> {
                b0 = (1f - cosW0) * 0.5f
                b1 = 1f - cosW0
                b2 = (1f - cosW0) * 0.5f
            }
            Type.HighPass -> {
                b0 = (1f + cosW0) * 0.5f
                b1 = -1f - cosW0
                b2 = (1f + cosW0) * 0.5f
            }
            Type.BandPass -> {
                b0 = r * alpha
                b1 = 0f
                b2 = -r * alpha
            }
            Type.Notch -> {
                b0 = 1f
                b1 = -2f * cosW0
                b2 = 1f
            }
        }

        b0a0 = b0 / a0
        b1a0 = b1 / a0
        b2a0 = b2 / a0
        a1a0 = a1 / a0
        a2a0 = a2 / a0
    }

    override fun process(sampleCount: Int, controls: Controls) {
        val inputBuffer = input.getConnectedOutputBuffer(0, sampleCount)
        val frequencyCVBuffer = frequencyCV.getConnectedOutputBuffer(0, sampleCount)
        val resonanceCVBuffer = resonanceCV.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)

        val frequencyCenter = controls.frequency.value
        val frequencyCV = controls.frequencyCV.value
        val resonanceCenter = controls.resonance.value
        val resonanceCV = controls.resonanceCV.value

        for (i in 0 until sampleCount) {
            val x0 = inputBuffer[i]

            if (firstSample) {
                y1 = x0
                y2 = x0
                x1 = x0
                x2 = x0
                firstSample = false
            }

            val frequency = frequencyCenter + frequencyCVBuffer[i] * frequencyCV
            val resonance = resonanceCenter + resonanceCVBuffer[i] * resonanceCV
            updateCoefficients(frequency, resonance)

            val y0 = b0a0 * x0 + b1a0 * x1 + b2a0 * x2 - a1a0 * y1 - a2a0 * y2

            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0

            outputBuffer[i] = y0
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(inputPort, input)
        visit(frequencyCVPort, frequencyCV)
        visit(resonanceCVPort, resonanceCV)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}