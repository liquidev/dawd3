package net.liquidev.dawd3.block.devices.oscillator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.device.NoControls
import net.liquidev.dawd3.audio.devices.oscillator.PulseOscillatorDevice
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object PulseOscillatorBlockDescriptor : DeviceBlockDescriptor<PulseOscillatorBlockDescriptor.ClientState, NoControls> {
    override val id = Identifier(Mod.id, "pulse_oscillator")

    override val portLayout = PhysicalPort.layout {
        port(
            PulseOscillatorDevice.phasePort,
            position = Vec2f(0.25f, 0.75f),
            side = PhysicalPort.Side.Front,
        )
        port(
            PulseOscillatorDevice.outputPort,
            position = Vec2f(0.75f, 0.75f),
            side = PhysicalPort.Side.Front
        )
    }

    class ClientState(controls: NoControls) : DeviceBlockDescriptor.ClientState {
        override val logicalDevice = DeviceInstance.create(PulseOscillatorDevice(), controls)
    }

    override fun initControls() = NoControls

    override fun onClientLoad(controls: NoControls, world: ClientWorld) = ClientState(controls)
}