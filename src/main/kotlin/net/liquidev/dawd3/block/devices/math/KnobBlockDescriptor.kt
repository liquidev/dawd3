package net.liquidev.dawd3.block.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.math.ConstantDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.block.device.descriptor.FaceTextures
import net.liquidev.dawd3.common.Cuboids
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Widget
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object KnobBlockDescriptor : DeviceBlockDescriptor<KnobBlockDescriptor.ClientState, ConstantDevice.Controls> {
    override val id = Identifier(Mod.id, "knob")

    override val cuboid = Cuboids.fullBlock
    override val portLayout = PhysicalPort.layout {
        port(
            ConstantDevice.outputPort,
            instanceName = "front",
            position = Vec2f(0.5f, 0.8125f),
            side = PhysicalPort.Side.Front,
        )
    }
    override val faceTextures = FaceTextures.withFrontAndSide { id }

    class ClientState(controls: ConstantDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(ConstantDevice(), controls)
    }

    override fun initControls() = ConstantDevice.Controls()

    override fun onClientLoad(controls: ConstantDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<ConstantDevice.Controls> {
        override fun open(controls: ConstantDevice.Controls, x: Int, y: Int): Widget =
            Window(x, y, 48, 48, Text.translatable("block.dawd3.knob")).apply {
                children.add(
                    Knob(
                        x = 14,
                        y = 18,
                        control = controls.value,
                        min = -48f,
                        max = 48f,
                        color = Knob.Color.Blue
                    )
                )
            }
    }
}