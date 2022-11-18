package net.liquidev.dawd3.block

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.liquidev.dawd3.D3Registry
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.item.Items
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Blocks {
    var blockRegistry = object : D3Registry<Block>() {
        override fun doRegister(identifier: Identifier, item: Block) {
            Registry.register(Registry.BLOCK, identifier, item)
        }
    }

    val speaker = add("speaker", SpeakerBlock(moduleBlockSettings()))
    val speakerEntity = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        Identifier(Mod.id, "speaker"),
        FabricBlockEntityTypeBuilder.create(::SpeakerBlockEntity, speaker.item).build(),
    )

    private fun moduleBlockSettings() = FabricBlockSettings
        .of(Material.METAL)
        .hardness(5.0f)
        .resistance(6.0f)

    private fun add(name: String, block: Block): D3Registry.Registered<Block> {
        Items.addItem(name, BlockItem(block, FabricItemSettings().group(Items.creativeTab)))
        return blockRegistry.add(name, block)
    }
}