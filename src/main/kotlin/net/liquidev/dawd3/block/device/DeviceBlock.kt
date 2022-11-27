package net.liquidev.dawd3.block.device

import net.liquidev.dawd3.common.*
import net.liquidev.dawd3.item.PatchCableItem
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3f
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class DeviceBlock(private val descriptor: AnyDeviceBlockDescriptor) :
    BlockWithEntity(descriptor.blockSettings),
    BlockEntityProvider {

    val outlineCuboids = HorizontalDirection.values().map { direction ->
        val cuboid = descriptor.cuboid
        val centerOrigin = Vec3f(0.5f, 0.5f, 0.5f)
        val from = cuboid.fromF - centerOrigin
        val to = cuboid.toF - centerOrigin
        Cuboids.newF(direction.rotateY(from) + centerOrigin, direction.rotateY(to) + centerOrigin)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(context: ItemPlacementContext): BlockState =
        defaultState.with(
            Properties.HORIZONTAL_FACING,
            context.playerFacing.opposite
        )

    @Deprecated("do not call this function directly")
    override fun getRenderType(state: BlockState): BlockRenderType =
        BlockRenderType.MODEL

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        DeviceBlockEntity.factory(descriptor).create(pos, state)

    @Deprecated("do not call this function directly")
    override fun onStateReplaced(
        state: BlockState,
        world: World,
        position: BlockPos,
        newState: BlockState,
        moved: Boolean,
    ) {
        PatchCableItem.onBlockDestroyed(position)

        @Suppress("DEPRECATION")
        super.onStateReplaced(state, world, position, newState, moved)
    }

    @Deprecated("do not call this function directly")
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext,
    ): VoxelShape {
        val direction = HorizontalDirection.fromDirection(state[Properties.HORIZONTAL_FACING])!!
        return VoxelShapes.cuboid(outlineCuboids[direction.index])
    }
}