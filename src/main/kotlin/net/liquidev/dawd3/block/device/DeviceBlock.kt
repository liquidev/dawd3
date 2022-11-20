package net.liquidev.dawd3.block.device

import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos

class DeviceBlock(private val descriptor: AnyDeviceBlockDescriptor) :
    BlockWithEntity(descriptor.blockSettings),
    BlockEntityProvider {

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(context: ItemPlacementContext): BlockState =
        defaultState.with(Properties.HORIZONTAL_FACING, context.playerFacing.opposite)

    @Deprecated("do not call directly")
    override fun getRenderType(state: BlockState): BlockRenderType =
        BlockRenderType.MODEL

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        DeviceBlockEntity.factory(descriptor).create(pos, state)
}