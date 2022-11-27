package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.device.PortName
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

data class ConnectPorts(
    val fromPosition: BlockPos,
    val fromPort: String,
    val toPosition: BlockPos,
    val toPort: String,
    val color: Byte,
) {
    companion object {
        private val logger = Mod.logger<ConnectPorts>()

        val id = Identifier(Mod.id, "connect_ports")

        fun registerClientReceiver() {
            ClientPlayNetworking.registerGlobalReceiver(id) { client, _, buffer, _ ->
                val packet = deserialize(buffer)
                client.execute {
                    val world = client.world
                    if (world != null) {
                        packet.handleInWorld(world)
                    } else {
                        logger.warn("packet received without a client world")
                    }
                }
            }
        }

        private fun deserialize(buffer: PacketByteBuf): ConnectPorts {
            return ConnectPorts(
                fromPosition = buffer.readBlockPos(),
                fromPort = buffer.readString(),
                toPosition = buffer.readBlockPos(),
                toPort = buffer.readString(),
                color = buffer.readByte(),
            )
        }
    }

    fun serialize(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeBlockPos(fromPosition)
        buffer.writeString(fromPort)
        buffer.writeBlockPos(toPosition)
        buffer.writeString(toPort)
        buffer.writeByte(color.toInt()) // bruh
        return buffer
    }

    private fun handleInWorld(world: World) {
        val fromBlockEntity = world.getBlockEntity(fromPosition)
        val toBlockEntity = world.getBlockEntity(toPosition)
        if (fromBlockEntity !is DeviceBlockEntity || toBlockEntity !is DeviceBlockEntity) {
            logger.warn("packet received with block entities not being device block entities")
            return
        }

        val fromPortName = PortName.fromString(fromPort)
        if (fromPortName == null) {
            logger.warn("port $fromPort does not exist on the from-device")
            return
        }
        val toPortName = PortName.fromString(toPort)
        if (toPortName == null) {
            logger.warn("port $toPort does not exist on the to-device")
            return
        }

        logger.debug("connecting logical device ports ($fromBlockEntity):($fromPortName) -> ($toBlockEntity):($toPortName)")
        DeviceBlockEntity.connectLogicalDevices(
            fromBlockEntity,
            fromPortName,
            toBlockEntity,
            toPortName,
            color,
        )
    }
}