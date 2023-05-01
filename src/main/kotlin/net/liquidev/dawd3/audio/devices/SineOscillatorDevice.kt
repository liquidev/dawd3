package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.AudioBuffer
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.audio.unit.VOct
import net.minecraft.util.Identifier
import kotlin.math.sin

private const val twoPi = 2.0f * kotlin.math.PI.toFloat()

class SineOscillatorDevice : Device<NoControls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "sine_oscillator")
        val frequencyCVPort = InputPortName(id, "frequency_cv")
        val outputPort = OutputPortName(id, "output")
    }

    private val frequencyCV = InputPort()
    private val output = OutputPort(bufferCount = 1)

    private val frequencyBuffer = AudioBuffer()
    private var phase = 0.0f

    override fun process(sampleCount: Int, channels: Int, controls: NoControls) {
        val voctBuffer = frequencyCV.getConnectedOutputBuffer(0, sampleCount)
        val frequencyBuffer = frequencyBuffer.getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            frequencyBuffer[i] = VOct(voctBuffer[i]).toFrequency(Audio.a4).value
        }

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