package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.net.serialization.readOptionalUuid
import net.liquidev.dawd3.net.serialization.writeOptionalUuid
import net.liquidev.dawd3.ui.widget.rack.shelves.ShelfEditing
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*

/**
 * Assign a shelf and shelf order to a block at the given position.
 *
 * When sent C2S, the data of the device block entity at the given position is modified to write
 * back its shelf UUID and shelf order. The server then propagates the shelf order to every block
 * entity in the rack and sends this packet S2C to all witnesses of the propagation.
 */
data class EditRack(
    val blockPosition: BlockPos,
    val shelf: UUID?,
    val shelfOrder: Array<UUID>,
) {
    companion object {
        val id = Identifier(Mod.id, "edit_rack")

        fun registerServerReceiver() {
            ServerPlayNetworking.registerGlobalReceiver(id) { server, player, _, buffer, _ ->
                val packet = deserialize(buffer)
                server.execute {
                    val blockState = player.world.getBlockState(packet.blockPosition)
                    val blockEntity =
                        player.world.getBlockEntity(packet.blockPosition) as? DeviceBlockEntity
                            ?: return@execute
                    blockEntity.shelf = packet.shelf
                    ShelfEditing.propagateShelfOrderData(
                        player.world,
                        packet.blockPosition,
                        packet.shelfOrder,
                    )
                    blockEntity.markDirty()

                    player.world.updateListeners(
                        packet.blockPosition,
                        blockState,
                        blockState,
                        Block.NOTIFY_LISTENERS,
                    )
                    for (witness in PlayerLookup.tracking(blockEntity)) {
                        if (witness != player) {
                            ServerPlayNetworking.send(witness, id, buffer)
                        }
                    }
                }
            }
        }

        fun registerClientReceiver() {

        }

        private fun deserialize(buffer: PacketByteBuf) =
            EditRack(
                blockPosition = buffer.readBlockPos(),
                shelf = buffer.readOptionalUuid(),
                shelfOrder = Array(buffer.readVarInt()) { buffer.readUuid() }
            )
    }

    fun serialize(): PacketByteBuf =
        PacketByteBufs.create()
            .writeBlockPos(blockPosition)
            .writeOptionalUuid(shelf)
            .writeVarInt(shelfOrder.size)
            .apply {
                for (shelfUuid in shelfOrder) {
                    writeUuid(shelfUuid)
                }
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EditRack

        if (blockPosition != other.blockPosition) return false
        if (shelf != other.shelf) return false
        return shelfOrder.contentEquals(other.shelfOrder)
    }

    override fun hashCode(): Int {
        var result = blockPosition.hashCode()
        result = 31 * result + (shelf?.hashCode() ?: 0)
        result = 31 * result + shelfOrder.contentHashCode()
        return result
    }

}