package net.liquidev.dawd3.block.devices.envelope

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.envelope.AdsrDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.ui.units.PercentageValue
import net.liquidev.dawd3.ui.units.SiValue
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object AdsrBlockDescriptor : DeviceBlockDescriptor<AdsrBlockDescriptor.ClientState, AdsrDevice.Controls> {
    override val id = Identifier(Mod.id, "adsr")

    override val portLayout = PhysicalPort.layout {
        port(AdsrDevice.triggerPort, position = Vec2f(0.25f, 0.5f), PhysicalPort.Side.Front)
        port(AdsrDevice.envelopePort, position = Vec2f(0.75f, 0.5f), PhysicalPort.Side.Front)
    }

    class ClientState(controls: AdsrDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(AdsrDevice(), controls)
    }

    override fun initControls() = AdsrDevice.Controls()

    override fun onClientLoad(controls: AdsrDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<AdsrDevice.Controls> {
        override fun open(controls: AdsrDevice.Controls, x: Int, y: Int) =
            Window(x, y, width = 121, height = 48, Text.translatable("block.dawd3.adsr")).apply {
                children.add(
                    Knob(
                        x = 8, y = 18,
                        controls.attack,
                        min = 0f, max = 8f,
                        Knob.Color.Yellow,
                        unit = SiValue.time,
                    )
                )
                children.add(
                    Knob(
                        x = 36, y = 18,
                        controls.decay,
                        min = 0f, max = 8f,
                        Knob.Color.Orange,
                        unit = SiValue.time,
                    )
                )
                children.add(
                    Knob(
                        x = 64, y = 18,
                        controls.sustain,
                        min = 0f, max = 1f,
                        Knob.Color.Red,
                        unit = PercentageValue
                    )
                )
                children.add(
                    Knob(
                        x = 92, y = 18,
                        controls.release,
                        min = 0f, max = 8f,
                        Knob.Color.Purple,
                        unit = SiValue.time,
                    )
                )
            }
    }
}