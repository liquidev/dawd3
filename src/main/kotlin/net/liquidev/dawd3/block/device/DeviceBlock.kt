package net.liquidev.dawd3.block.device

import net.liquidev.dawd3.common.*
import net.liquidev.dawd3.item.PatchCableItem
import net.liquidev.dawd3.ui.Rack
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
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
        PatchCableItem.removeAllConnectionsAtBlock(position)

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

    @Deprecated("do not call this function directly")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult,
    ): ActionResult {
        val blockEntity =
            world.getBlockEntity(pos) as? DeviceBlockEntity ?: return ActionResult.PASS

        // We have to check this event ahead of time (in addition to doing it in PatchCableItem)
        // because block interactions take priority over item interactions.
        val usedPortName = DeviceBlockInteractions.findUsedPort(hit, blockEntity.descriptor)
        // TODO: Right-clicking the port should pop out its patch cable.

        if (usedPortName == null && world is ClientWorld && !player.isSneaking) {
            val rack = Rack(world, Rack.collectAdjacentDevices(world, pos))
            if (rack.hasOpenWindows()) {
                MinecraftClient.getInstance().setScreen(rack)
                return ActionResult.SUCCESS
            }
        }
        if (usedPortName != null && player.isSneaking) {
            return if (blockEntity.severConnectionsInPort(usedPortName)) {
                ActionResult.SUCCESS
            } else {
                ActionResult.PASS
            }
        }

        return ActionResult.PASS
    }
}