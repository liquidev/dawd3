package net.liquidev.dawd3.block.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.device.NoControls
import net.liquidev.dawd3.audio.devices.oscillator.SineOscillatorDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object SineOscillatorBlockDescriptor : DeviceBlockDescriptor<SineOscillatorBlockDescriptor.ClientState, NoControls> {
    override val id = Identifier(Mod.id, "sine_oscillator")

    override val portLayout = PhysicalPort.layout {
        port(
            SineOscillatorDevice.phasePort,
            position = Vec2f(0.25f, 0.5f),
            side = PhysicalPort.Side.Front,
        )
        port(
            SineOscillatorDevice.outputPort,
            position = Vec2f(0.75f, 0.5f),
            side = PhysicalPort.Side.Front
        )
    }

    class ClientState(controls: NoControls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(SineOscillatorDevice(), controls)
    }

    override fun initControls() = NoControls

    override fun onClientLoad(controls: NoControls, world: ClientWorld) = ClientState(controls)
}