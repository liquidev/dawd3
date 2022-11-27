package net.liquidev.dawd3.block.devices

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.device.DeviceInstance
import net.liquidev.dawd3.audio.devices.TerminalDevice
import net.liquidev.dawd3.audio.generator.DeviceGraphGenerator
import net.liquidev.dawd3.audio.generator.MixGenerator
import net.liquidev.dawd3.block.device.DeviceBlockDescriptor
import net.liquidev.dawd3.block.device.PhysicalPort
import net.minecraft.block.Material
import net.minecraft.client.world.ClientWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f

object SpeakerBlockDescriptor : DeviceBlockDescriptor<SpeakerBlockDescriptor.ClientState, Unit> {
    override val id = Identifier(Mod.id, "speaker")
    override val blockSettings = FabricBlockSettings
        .of(Material.WOOD)
        .hardness(2.0f).resistance(6.0f)
        .sounds(BlockSoundGroup.WOOD)!!

    override val portLayout = PhysicalPort.layout {
        port(TerminalDevice.inputPort, position = Vec2f(0.5f, 0.75f), side = PhysicalPort.Side.Back)
    }

    class ClientState : DeviceBlockDescriptor.ClientState {
        internal val channel: MixGenerator.Channel<DeviceGraphGenerator>
        override val logicalDevice: DeviceInstance

        init {
            val generator = DeviceGraphGenerator()
            channel = Audio.mixer.createChannel(generator)
            val terminal = generator.terminalDevice
            logicalDevice = terminal
        }
    }

    override fun onClientLoad(world: ClientWorld) = ClientState()

    override fun onClientUnload(state: ClientState, world: ClientWorld) {
        state.channel.stop()
    }
}
