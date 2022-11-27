package net.liquidev.dawd3.block.devices

import FaceTextures
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.ConstantDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.common.Cuboids
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object FaderBlockDescriptor : DeviceBlockDescriptor<FaderBlockDescriptor.ClientState, Unit> {
    override val id = Identifier(Mod.id, "fader")

    override val cuboid = Cuboids.halfBlock
    override val portLayout = PhysicalPort.layout {
        port(
            ConstantDevice.outputPort,
            instanceName = "front",
            position = Vec2f(0.5f, 0.75f),
            side = PhysicalPort.Side.Front,
        )
    }
    override val faceTextures = FaceTextures.withTopSideAndBottom { id }

    class ClientState : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance(ConstantDevice(value = 440.0f))
    }

    override fun onClientLoad(world: ClientWorld) = ClientState()
}