package net.liquidev.dawd3.block.entity

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.world.ClientWorld
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.math.BlockPos

abstract class D3BlockEntity(
    type: BlockEntityType<out D3BlockEntity>,
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(type, pos, state) {

    open fun onClientLoad(world: ClientWorld) {}
    open fun onClientUnload(world: ClientWorld) {}

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return createNbt()
    }
}
