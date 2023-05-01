package net.liquidev.dawd3.net

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

/** C2S message that a block's save data needs to be marked dirty as a result of control tweaks. */
data class ControlTweaked(val position: BlockPos) {
    companion object {
        val id = Identifier(Mod.id, "control_tweaked")

        fun registerServerReceiver() {
            ServerPlayNetworking.registerGlobalReceiver(id) { server, player, _, buffer, _ ->
                val packet = deserialize(buffer)
                server.execute {
                    val world = player.world
                    if (world != null) {
                        val blockEntity =
                            world.getBlockEntity(packet.position) as? DeviceBlockEntity
                                ?: return@execute
                        blockEntity.markDirty()
                    }
                }
            }
        }

        private fun deserialize(buffer: PacketByteBuf) =
            ControlTweaked(
                position = buffer.readBlockPos(),
            )
    }

    fun serialize(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeBlockPos(position)
        return buffer
    }
}