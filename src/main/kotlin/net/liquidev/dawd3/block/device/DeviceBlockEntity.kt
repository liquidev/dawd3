package net.liquidev.dawd3.block.device

import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.block.entity.D3BlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

private typealias DeviceBlockFactory = FabricBlockEntityTypeBuilder.Factory<DeviceBlockEntity>

class DeviceBlockEntity(
    type: BlockEntityType<DeviceBlockEntity>,
    blockPos: BlockPos,
    blockState: BlockState,
    private val descriptor: AnyDeviceBlockDescriptor,
) : D3BlockEntity(type, blockPos, blockState) {
    companion object {
        fun factory(descriptor: AnyDeviceBlockDescriptor): DeviceBlockFactory =
            DeviceBlockFactory { blockPos, blockState ->
                val type by lazy { Blocks.deviceBlocks[descriptor.id]!!.blockEntity }
                DeviceBlockEntity(type, blockPos, blockState, descriptor)
            }
    }

    private var clientState: DeviceBlockDescriptor.ClientState? = null
    private var serverState: Any? = null

    override fun onClientLoad(world: ClientWorld) {
        clientState = descriptor.onClientLoad(world)
    }

    override fun onClientUnload(world: ClientWorld) {
        val clientState = clientState
        if (clientState != null) {
            descriptor.onClientUnload(clientState, world)
        }
    }
}