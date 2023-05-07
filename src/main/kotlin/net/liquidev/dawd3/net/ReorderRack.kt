package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

data class ReorderRack(val entries: List<Entry>) {
    data class Entry(val blockPosition: BlockPos, val sortPriority: Int)

    companion object {
        val id = Identifier(Mod.id, "reorder_rack")

        fun registerServerReceiver() {
            ServerPlayNetworking.registerGlobalReceiver(id) { server, player, _, buffer, _ ->
                val packet = deserialize(buffer)
                server.execute {
                    for (entry in packet.entries) {
                        val blockEntity =
                            player.world.getBlockEntity(entry.blockPosition) as? DeviceBlockEntity
                                ?: continue
                        blockEntity.sortPriority = entry.sortPriority
                        blockEntity.markDirty()

                        val blockState = player.world.getBlockState(entry.blockPosition)
                        player.world.updateListeners(
                            entry.blockPosition,
                            blockState,
                            blockState,
                            Block.NOTIFY_LISTENERS
                        )
                        for (witness in PlayerLookup.tracking(blockEntity)) {
                            if (witness != player) {
                                ServerPlayNetworking.send(witness, id, buffer)
                            }
                        }
                    }
                }
            }
        }

        private fun deserialize(buffer: PacketByteBuf) =
            ReorderRack(
                entries = MutableList(buffer.readVarInt()) {
                    Entry(
                        blockPosition = buffer.readBlockPos(),
                        sortPriority = buffer.readVarInt(),
                    )
                }
            )
    }

    fun serialize(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeVarInt(entries.size)
        for (entry in entries) {
            buffer.writeBlockPos(entry.blockPosition)
            buffer.writeVarInt(entry.sortPriority)
        }
        return buffer
    }
}