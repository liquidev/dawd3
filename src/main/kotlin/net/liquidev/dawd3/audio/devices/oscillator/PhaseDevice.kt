package net.liquidev.dawd3.audio.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.AudioBuffer
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.audio.math.Trigger
import net.liquidev.dawd3.audio.unit.VOct
import net.minecraft.util.Identifier

class PhaseDevice : Device<NoControls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "phase")
        val frequencyCVPort = InputPortName(id, "frequency_cv")
        val resetPort = InputPortName(id, "reset")
        val outputPort = OutputPortName(id, "output")
    }

    private val frequencyCV = InputPort()
    private val reset = InputPort()
    private val output = OutputPort(bufferCount = 1)

    private val frequencyBuffer = AudioBuffer()
    private var phase = 0.0f
    private val resetTrigger = Trigger()

    override fun process(sampleCount: Int, controls: NoControls) {
        val voctBuffer = frequencyCV.getConnectedOutputBuffer(0, sampleCount)
        val frequencyBuffer = frequencyBuffer.getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            frequencyBuffer[i] = VOct(voctBuffer[i]).toFrequency(Audio.a4).value
        }

        val resetBuffer = reset.getConnectedOutputBuffer(0, sampleCount)
        val outputBuffer = output.buffers[0].getOrReallocate(sampleCount)
        for (i in 0 until sampleCount) {
            val phaseStep = Audio.sampleRateFInv * frequencyBuffer[i]
            phase += phaseStep
            phase %= 1f

            if (resetTrigger.stepEdge(resetBuffer[i]) == Trigger.Edge.Rising) {
                phase = 0f
            }

            outputBuffer[i] = phase
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(frequencyCVPort, frequencyCV)
        visit(resetPort, reset)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(outputPort, output)
    }
}