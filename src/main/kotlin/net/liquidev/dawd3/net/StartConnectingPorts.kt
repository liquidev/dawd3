package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.item.PatchCableItem
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class StartConnectingPorts(val blockPosition: BlockPos, val portName: String, val color: Byte) {
    companion object {
        private val logger = Mod.logger<ConnectPorts>()

        val id = Identifier(Mod.id, "start_connecting_ports")

        fun registerClientReceiver() {
            ClientPlayNetworking.registerGlobalReceiver(id) { client, _, buffer, _ ->
                val packet = deserialize(buffer)
                client.execute {
                    val player = client.player
                    if (player != null) {
                        packet.handleForPlayer(player)
                    } else {
                        logger.warn("packet received without a client player")
                    }
                }
            }
        }

        private fun deserialize(buffer: PacketByteBuf): StartConnectingPorts {
            return StartConnectingPorts(
                blockPosition = buffer.readBlockPos(),
                portName = buffer.readString(),
                color = buffer.readByte(),
            )
        }
    }

    fun serialize(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeBlockPos(blockPosition)
        buffer.writeString(portName)
        buffer.writeByte(color.toInt())
        return buffer
    }

    private fun handleForPlayer(player: ClientPlayerEntity) {
        val portName = PortName.fromString(portName)
        if (portName == null) {
            logger.warn("invalid port name ${this.portName}")
            return
        }

        PatchCableItem.startConnecting(
            player,
            PatchCableItem.OngoingConnection(blockPosition, portName, color)
        )
    }
}