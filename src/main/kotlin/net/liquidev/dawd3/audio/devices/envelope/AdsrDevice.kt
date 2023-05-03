package net.liquidev.dawd3.audio.devices.envelope

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.device.*
import net.liquidev.dawd3.audio.math.Trigger
import net.minecraft.util.Identifier
import kotlin.math.max

class AdsrDevice : Device<AdsrDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "adsr")

        val attackControl = ControlDescriptor(id, "attack", 0.02f)
        val decayControl = ControlDescriptor(id, "decay", 0.1f)
        val sustainControl = ControlDescriptor(id, "sustain", 0.8f)
        val releaseControl = ControlDescriptor(id, "release", 0.5f)

        val triggerPort = InputPortName(id, "trigger")
        val envelopePort = OutputPortName(id, "envelope")
    }

    class Controls : ControlSet {
        val attack = FloatControl(attackControl)
        val decay = FloatControl(decayControl)
        val sustain = FloatControl(sustainControl)
        val release = FloatControl(releaseControl)

        override fun visitControls(visit: (ControlName, Control) -> Unit) {
            visit(attackControl.name, attack)
            visit(decayControl.name, decay)
            visit(sustainControl.name, sustain)
            visit(releaseControl.name, release)
        }
    }

    private enum class State {
        Attack,
        Decay,
        Release,
    }

    val trigger = InputPort()
    val envelope = OutputPort(bufferCount = 1)

    private val triggerDetector = Trigger()
    private var envelopeValue = 0f
    private var state = State.Release

    override fun process(sampleCount: Int, controls: Controls) {
        val triggerBuffer = trigger.getConnectedOutputBuffer(0, sampleCount)
        val envelopeBuffer = envelope.buffers[0].getOrReallocate(sampleCount)

        val attack = controls.attack.value
        val decay = controls.decay.value
        val sustain = controls.sustain.value
        val release = controls.release.value

        for (i in 0 until sampleCount) {
            when (triggerDetector.stepEdge(triggerBuffer[i])) {
                Trigger.Edge.Constant -> {}
                Trigger.Edge.Rising -> state = State.Attack
                Trigger.Edge.Falling -> state = State.Release
            }
            val envelopeDelta = when (state) {
                State.Attack -> Audio.sampleRateFInv / attack
                State.Decay -> Audio.sampleRateFInv / -decay
                State.Release -> Audio.sampleRateFInv / -release
            }
            envelopeValue += envelopeDelta
            envelopeValue = when (state) {
                State.Attack -> if (envelopeValue >= 1f) {
                    state = State.Decay
                    1f
                } else {
                    envelopeValue
                }
                State.Decay -> max(envelopeValue, sustain)
                State.Release -> max(envelopeValue, 0f)
            }
            envelopeBuffer[i] = envelopeValue
        }
    }

    override fun visitInputPorts(visit: (InputPortName, InputPort) -> Unit) {
        visit(triggerPort, trigger)
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(envelopePort, envelope)
    }
}