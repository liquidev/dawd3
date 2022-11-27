package net.liquidev.dawd3.block.devices

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.SineOscillatorDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object SineOscillatorBlockDescriptor : DeviceBlockDescriptor<SineOscillatorBlockDescriptor.ClientState, Unit> {
    override val id = Identifier(Mod.id, "sine_oscillator")

    override val portLayout = PhysicalPort.layout {
        port(
            SineOscillatorDevice.frequencyCVPort,
            position = Vec2f(0.25f, 0.5f),
            side = PhysicalPort.Side.Front,
        )
        port(
            SineOscillatorDevice.outputPort,
            position = Vec2f(0.75f, 0.5f),
            side = PhysicalPort.Side.Front
        )
    }

    class ClientState : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance(SineOscillatorDevice())
    }

    override fun onClientLoad(world: ClientWorld) = ClientState()
}