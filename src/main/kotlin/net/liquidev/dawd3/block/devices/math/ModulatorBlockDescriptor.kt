package net.liquidev.dawd3.block.devices.math

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.math.FmaDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.block.device.descriptor.FaceTextures
import net.liquidev.dawd3.common.Cuboids
import net.liquidev.dawd3.ui.widget.Knob
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object ModulatorBlockDescriptor : DeviceBlockDescriptor<ModulatorBlockDescriptor.ClientState, FmaDevice.Controls> {
    override val id = Identifier(Mod.id, "modulator")

    override val cuboid = Cuboids.fullBlock
    override val portLayout = PhysicalPort.layout {
        port(
            FmaDevice.inputPort,
            position = Vec2f(0.25f, 0.5f),
            side = PhysicalPort.Side.Front,
        )
        port(
            FmaDevice.outputPort,
            position = Vec2f(0.75f, 0.5f),
            side = PhysicalPort.Side.Front,
        )
    }
    override val faceTextures = FaceTextures.withFrontAndSide { id }

    class ClientState(controls: FmaDevice.Controls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(FmaDevice(), controls)
    }

    override fun initControls() = FmaDevice.Controls()

    override fun onClientLoad(controls: FmaDevice.Controls, world: ClientWorld) =
        ClientState(controls)

    override val ui = object : DeviceBlockDescriptor.UI<FmaDevice.Controls> {
        override fun open(controls: FmaDevice.Controls, x: Float, y: Float) =
            Window(x, y, 78f, 48f, Text.translatable("block.dawd3.modulator")).apply {
                children.add(
                    Knob(
                        x = 14f,
                        y = 18f,
                        control = controls.add,
                        min = -48f,
                        max = 48f,
                        color = Knob.Color.Blue,
                    )
                )
                children.add(
                    Knob(
                        x = 42f,
                        y = 18f,
                        control = controls.multiply,
                        min = -8f,
                        max = 8f,
                        color = Knob.Color.Green,
                    )
                )
            }
    }

}