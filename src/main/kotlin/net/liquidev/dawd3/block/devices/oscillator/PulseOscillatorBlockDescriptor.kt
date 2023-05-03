package net.liquidev.dawd3.block.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.oscillator.PulseOscillatorDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.ui.units.PercentageValue
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object PulseOscillatorBlockDescriptor : DeviceBlockDescriptor<PulseOscillatorBlockDescriptor.ClientState, PulseOscillatorDevice.Controls> {
    override val id = Identifier(Mod.id, "pulse_oscillator")

    override val portLayout = PhysicalPort.layout {
        port(
            PulseOscillatorDevice.phasePort,
            position = Vec2f(0.25f, 0.25f),
            PhysicalPort.Side.Front
        )
        port(
            PulseOscillatorDevice.dutyCyclePort,
            position = Vec2f(0.25f, 0.75f),
            PhysicalPort.Side.Front
        )
        port(
            PulseOscillatorDevice.outputPort,
            position = Vec2f(0.75f, 0.5f),
            PhysicalPort.Side.Front
        )
    }

    class ClientState(controls: PulseOscillatorDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(PulseOscillatorDevice(), controls)
    }

    override fun initControls() = PulseOscillatorDevice.Controls()

    override fun onClientLoad(controls: PulseOscillatorDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<PulseOscillatorDevice.Controls> {
        override fun open(controls: PulseOscillatorDevice.Controls, x: Int, y: Int) =
            Window(
                x,
                y,
                width = 78,
                height = 48,
                Text.translatable("block.dawd3.pulse_oscillator")
            ).apply {
                children.add(
                    Knob(
                        x = 14,
                        y = 18,
                        control = controls.dutyCycle,
                        min = 0f,
                        max = 1f,
                        color = Knob.Color.Blue,
                        unit = PercentageValue,
                    )
                )
                children.add(
                    Knob(
                        x = 42,
                        y = 18,
                        control = controls.dutyCycleCV,
                        min = 0f,
                        max = 1f,
                        color = Knob.Color.Green,
                        unit = PercentageValue,
                    )
                )
            }
    }
}