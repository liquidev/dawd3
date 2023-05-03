package net.liquidev.dawd3.block.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.math.AmplifierDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.ui.units.AmplitudeValue
import net.liquidev.dawd3.ui.units.PercentageValue
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Widget
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object AmplifierBlockDescriptor : DeviceBlockDescriptor<AmplifierBlockDescriptor.ClientState, AmplifierDevice.Controls> {
    override val id = Identifier(Mod.id, "amplifier")
    override val portLayout = PhysicalPort.layout {
        port(
            AmplifierDevice.amplitudeCVPort,
            position = Vec2f(0.5f, 0.25f),
            PhysicalPort.Side.Front
        )
        port(
            AmplifierDevice.inputPort,
            position = Vec2f(0.25f, 0.75f),
            PhysicalPort.Side.Front
        )
        port(
            AmplifierDevice.outputPort,
            position = Vec2f(0.75f, 0.75f),
            PhysicalPort.Side.Front
        )
    }

    class ClientState(controls: AmplifierDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(AmplifierDevice(), controls)
    }

    override fun initControls() = AmplifierDevice.Controls()

    override fun onClientLoad(controls: AmplifierDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<AmplifierDevice.Controls> {
        override fun open(controls: AmplifierDevice.Controls, x: Int, y: Int): Widget =
            Window(
                x,
                y,
                width = 78,
                height = 48,
                Text.translatable("block.dawd3.amplifier")
            ).apply {
                children.add(
                    Knob(
                        x = 14,
                        y = 18,
                        control = controls.amplitude,
                        min = 0f,
                        max = 1f,
                        color = Knob.Color.Red,
                        unit = AmplitudeValue,
                    )
                )
                children.add(
                    Knob(
                        x = 42,
                        y = 18,
                        control = controls.amplitudeCV,
                        min = -8f,
                        max = 8f,
                        color = Knob.Color.Blue,
                        unit = PercentageValue,
                    )
                )
            }
    }
}