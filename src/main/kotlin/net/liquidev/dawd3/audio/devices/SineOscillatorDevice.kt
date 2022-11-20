package net.liquidev.dawd3.audio.devices

import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.device.Device
import net.liquidev.dawd3.audio.device.InputPort
import net.liquidev.dawd3.audio.device.OutputPort
import net.liquidev.dawd3.audio.device.PortName
import kotlin.math.sin

private const val twoPi = 2.0f * kotlin.math.PI.toFloat()

class SineOscillatorDevice : Device {
    object FrequencyCV : PortName()
    object Output : PortName()

    val frequencyCV = InputPort()
    val output = OutputPort(bufferCount = 1)

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

    override fun visitInputPorts(visit: (PortName, InputPort) -> Unit) {
        visit(FrequencyCV, frequencyCV)
    }

    override fun visitOutputPorts(visit: (PortName, OutputPort) -> Unit) {
        visit(Output, output)
    }
}