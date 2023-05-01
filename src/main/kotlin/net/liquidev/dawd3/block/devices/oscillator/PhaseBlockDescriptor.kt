package net.liquidev.dawd3.block.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.device.NoControls
import net.liquidev.dawd3.audio.devices.oscillator.PhaseDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.liquidev.dawd3.block.device.descriptor.FaceTextures
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object PhaseBlockDescriptor : DeviceBlockDescriptor<PhaseBlockDescriptor.ClientState, NoControls> {
    override val id = Identifier(Mod.id, "phase")

    override val portLayout = PhysicalPort.layout {
        port(
            PhaseDevice.frequencyCVPort,
            position = Vec2f(0.25f, 0.5f),
            side = PhysicalPort.Side.Front,
        )
        port(
            PhaseDevice.outputPort,
            position = Vec2f(0.75f, 0.5f),
            side = PhysicalPort.Side.Front
        )
    }
    override val faceTextures = FaceTextures.withFrontAndSide { id }

    class ClientState(controls: NoControls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(PhaseDevice(), controls)
    }

    override fun initControls() = NoControls

    override fun onClientLoad(controls: NoControls, world: ClientWorld) = ClientState(controls)
}