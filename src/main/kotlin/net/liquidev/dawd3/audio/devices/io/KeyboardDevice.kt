package net.liquidev.dawd3.audio.devices.io

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.*
import net.minecraft.util.Identifier

class KeyboardDevice : Device<KeyboardDevice.Controls> {
    companion object : DeviceDescriptor {
        override val id = Identifier(Mod.id, "keyboard")

        val pitchControl = ControlDescriptor(id, "pitch", 0f)
        val triggerControl = ControlDescriptor(id, "trigger", 0f)
        val velocityControl = ControlDescriptor(id, "velocity", 0f)

        val pitchOutput = OutputPortName(id, "pitch")
        val triggerOutput = OutputPortName(id, "trigger")
        val velocityOutput = OutputPortName(id, "velocity")
    }

    class Controls : ControlSet {
        val pitch = FloatControl(pitchControl)
        val trigger = FloatControl(triggerControl)
        val velocity = FloatControl(velocityControl)

        override fun visitControls(visit: (ControlName, Control) -> Unit) {
            visit(pitchControl.name, pitch)
            visit(triggerControl.name, trigger)
            visit(velocityControl.name, velocity)
        }
    }

    val pitch = OutputPort(bufferCount = 1)
    val trigger = OutputPort(bufferCount = 1)
    val velocity = OutputPort(bufferCount = 1)

    override fun process(sampleCount: Int, controls: Controls) {
        val pitchBuffer = pitch.buffers[0].getOrReallocate(sampleCount)
        val triggerBuffer = trigger.buffers[0].getOrReallocate(sampleCount)
        val velocityBuffer = velocity.buffers[0].getOrReallocate(sampleCount)

        val pitch = controls.pitch.value
        val trigger = controls.trigger.value
        val velocity = controls.velocity.value

        for (i in 0 until sampleCount) {
            pitchBuffer[i] = pitch
        }
        for (i in 0 until sampleCount) {
            triggerBuffer[i] = trigger
        }
        for (i in 0 until sampleCount) {
            velocityBuffer[i] = velocity
        }
    }

    override fun visitOutputPorts(visit: (OutputPortName, OutputPort) -> Unit) {
        visit(pitchOutput, pitch)
        visit(triggerOutput, trigger)
        visit(velocityOutput, velocity)
    }
}