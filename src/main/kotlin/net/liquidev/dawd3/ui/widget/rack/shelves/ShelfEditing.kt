package net.liquidev.dawd3.ui.widget.rack.shelves

import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

object ShelfEditing {
    fun propagateShelfOrderData(world: World, from: BlockPos, shelfOrder: Array<UUID>) {
        propagateShelfOrderDataRec(world, from, HashSet(), shelfOrder)
    }

    private fun propagateShelfOrderDataRec(
        world: World,
        position: BlockPos,
        traversed: HashSet<BlockPos>,
        sourceShelfOrder: Array<UUID>,
    ) {
        val thisBlockEntity = world.getBlockEntity(position) as? DeviceBlockEntity ?: return
        if (position !in traversed) {
            traversed.add(position)

            // Maybe not the fastest thing... but I don't want to share a single piece of mutable
            // data between all the block entities, as that can cause weird bugs later down the line.
            thisBlockEntity.shelfOrder.clear()
            thisBlockEntity.shelfOrder.addAll(sourceShelfOrder)
            thisBlockEntity.markDirty()

            val blockState = world.getBlockState(position)
            world.updateListeners(position, blockState, blockState, Block.NOTIFY_LISTENERS)

            propagateShelfOrderDataRec(world, position.up(), traversed, sourceShelfOrder)
            propagateShelfOrderDataRec(world, position.down(), traversed, sourceShelfOrder)
            propagateShelfOrderDataRec(world, position.north(), traversed, sourceShelfOrder)
            propagateShelfOrderDataRec(world, position.south(), traversed, sourceShelfOrder)
            propagateShelfOrderDataRec(world, position.east(), traversed, sourceShelfOrder)
            propagateShelfOrderDataRec(world, position.west(), traversed, sourceShelfOrder)
        }
    }
}