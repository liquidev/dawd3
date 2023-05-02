package net.liquidev.dawd3.block

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.liquidev.dawd3.D3Registry
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.device.AnyDeviceBlockDescriptor
import net.liquidev.dawd3.block.device.DeviceBlock
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.block.device.DeviceBlockEntityRenderer
import net.liquidev.dawd3.block.devices.SpeakerBlockDescriptor
import net.liquidev.dawd3.block.devices.math.AmplifierBlockDescriptor
import net.liquidev.dawd3.block.devices.math.KnobBlockDescriptor
import net.liquidev.dawd3.block.devices.math.ModulatorBlockDescriptor
import net.liquidev.dawd3.block.devices.oscillator.*
import net.liquidev.dawd3.item.Items
import net.minecraft.block.Block
import net.minecraft.block.Material
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

    // To not have to mess with rendering the vertices ourselves we just use a baked model that's
    // represented as a block.
    val patchCablePlug = Registry.register(
        Registry.BLOCK,
        Identifier(Mod.id, "patch_cable_plug"),
        Block(FabricBlockSettings.of(Material.METAL))
    )!!

    // Device blocks
    val speaker = registerDeviceBlock(SpeakerBlockDescriptor)

    val knob = registerDeviceBlock(KnobBlockDescriptor)
    val amplifier = registerDeviceBlock(AmplifierBlockDescriptor)
    val modulator = registerDeviceBlock(ModulatorBlockDescriptor)

    val phase = registerDeviceBlock(PhaseBlockDescriptor)
    val sineOscillator = registerDeviceBlock(SineOscillatorBlockDescriptor)
    val pulseOscillator = registerDeviceBlock(PulseOscillatorBlockDescriptor)
    val sawOscillator = registerDeviceBlock(SawOscillatorBlockDescriptor)
    val triangleOscillator = registerDeviceBlock(TriangleOscillatorBlockDescriptor)

    fun initialize() {}

    fun initializeClient() {
        for ((_, deviceBlock) in deviceBlocks) {
            BlockEntityRendererRegistry.register(
                deviceBlock.blockEntity,
                ::DeviceBlockEntityRenderer
            )
        }
    }
}