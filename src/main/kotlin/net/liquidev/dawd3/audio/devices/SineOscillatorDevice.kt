package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier
import kotlin.math.sin

private const val twoPi = 2.0f * kotlin.math.PI.toFloat()

class SineOscillatorDevice : Device {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "sine_oscillator")
        val frequencyCVPort = InputPortName(id, "frequency_cv")
        val outputPort = OutputPortName(id, "output")
    }

    private val frequencyCV = InputPort()
    private val output = OutputPort(bufferCount = 1)

    private var phase = 0.0f

    override fun process(sampleCount: Int, channels: Int) {
        val frequencyBuffer = frequencyCV.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            val phaseStep = Audio.sampleRateFInv * frequencyBuffer[i]
            phase += phaseStep
            phase %= 1.0f
            outputBuffer[i] = sin(phase * twoPi)
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(frequencyCVPort, frequencyCV)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}