package net.liquidev.dawd3.common

import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun World.syncBlockToClients(position: BlockPos) {
    val state = getBlockState(position)
    updateListeners(position, state, state, Block.NOTIFY_LISTENERS)
}
