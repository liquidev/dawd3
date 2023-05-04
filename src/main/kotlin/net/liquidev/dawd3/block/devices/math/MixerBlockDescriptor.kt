package net.liquidev.dawd3.block.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.math.MixDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.ui.units.AmplitudeValue
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object MixerBlockDescriptor : DeviceBlockDescriptor<MixerBlockDescriptor.ClientState, MixDevice.Controls> {
    override val id = Identifier(Mod.id, "mixer")

    override val portLayout = PhysicalPort.layout {
        port(MixDevice.aPort, position = Vec2f(0.25f, 0.25f), PhysicalPort.Side.Front)
        port(MixDevice.bPort, position = Vec2f(0.25f, 0.75f), PhysicalPort.Side.Front)
        port(MixDevice.outputPort, position = Vec2f(0.75f, 0.5f), PhysicalPort.Side.Front)
    }

    class ClientState(controls: MixDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(MixDevice(), controls)
    }

    override fun initControls() = MixDevice.Controls()

    override fun onClientLoad(controls: MixDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<MixDevice.Controls> {
        override fun open(controls: MixDevice.Controls, x: Float, y: Float) =
            Window(x, y, width = 78f, height = 48f, Text.translatable("block.dawd3.mixer")).apply {
                children.add(
                    Knob(
                        x = 14f,
                        y = 18f,
                        control = controls.aAmplitude,
                        min = 0f,
                        max = 1f,
                        color = Knob.Color.Red,
                        unit = AmplitudeValue,
                    )
                )
                children.add(
                    Knob(
                        x = 42f,
                        y = 18f,
                        control = controls.bAmplitude,
                        min = 0f,
                        max = 1f,
                        color = Knob.Color.Red,
                        unit = AmplitudeValue,
                    )
                )
            }
    }
}