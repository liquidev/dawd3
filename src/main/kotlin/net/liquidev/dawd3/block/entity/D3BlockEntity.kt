package net.liquidev.dawd3.block.entity

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

abstract class D3BlockEntity(
    type: BlockEntityType<out D3BlockEntity>,
    pos: BlockPos,
    state: BlockState,
) :
    BlockEntity(type, pos, state) {

    open fun onClientLoad(world: ClientWorld) {}
    open fun onClientUnload(world: ClientWorld) {}
}
