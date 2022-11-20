package net.liquidev.dawd3.block

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.liquidev.dawd3.D3Registry
import net.liquidev.dawd3.block.device.AnyDeviceBlockDescriptor
import net.liquidev.dawd3.block.device.DeviceBlock
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.block.devices.SpeakerBlockDescriptor
import net.liquidev.dawd3.item.Items
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Blocks {
    data class RegisteredDeviceBlock(
        val block: Block,
        val item: D3Registry.Registered<Items.RegisteredItem>,
        val blockEntity: BlockEntityType<DeviceBlockEntity>,
        val descriptor: AnyDeviceBlockDescriptor,
    )

    val deviceBlocks = hashMapOf<Identifier, RegisteredDeviceBlock>()

    fun registerDeviceBlock(descriptor: AnyDeviceBlockDescriptor): RegisteredDeviceBlock {
        val block =
            Registry.register(Registry.BLOCK, descriptor.id, DeviceBlock(descriptor))
        val item = Items.addItem(
            descriptor.id,
            BlockItem(block, FabricItemSettings().group(Items.creativeTab))
        )
        val blockEntity = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            descriptor.id,
            FabricBlockEntityTypeBuilder.create(
                DeviceBlockEntity.factory(descriptor), block
            ).build(),
        )
        val registeredDeviceBlock = RegisteredDeviceBlock(block, item, blockEntity, descriptor)
        deviceBlocks[descriptor.id] = registeredDeviceBlock
        return registeredDeviceBlock
    }

    // Device blocks
    val speaker = registerDeviceBlock(SpeakerBlockDescriptor)

    fun initialize() {}
}