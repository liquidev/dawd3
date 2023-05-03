package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

/** S2C packet notifying clients about a client disconnecting the cables from a port. */
data class DisconnectPort(
    val position: BlockPos,
    val port: Identifier,
) {
    companion object {
        val id = Identifier(Mod.id, "disconnect_port")

        fun registerClientReceiver() {
            ClientPlayNetworking.registerGlobalReceiver(id) { client, _, buffer, _ ->
                val packet = deserialize(buffer)
                client.execute {
                    val world = client.world ?: return@execute
                    val blockEntity = world.getBlockEntity(packet.position) as? DeviceBlockEntity
                        ?: return@execute
                    val portName = PortName.fromString(packet.port.toString()) ?: return@execute
                    blockEntity.severConnectionsInPort(portName)
                }
            }
        }

        private fun deserialize(buffer: PacketByteBuf) =
            DisconnectPort(
                position = buffer.readBlockPos(),
                port = buffer.readIdentifier(),
            )
    }

    fun serialize() = PacketByteBufs.create()
        .writeBlockPos(position)
        .writeIdentifier(port)
}