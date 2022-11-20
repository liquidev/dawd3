package net.liquidev.dawd3.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.datagen.device.DeviceBlockModel
import net.minecraft.data.client.*
import net.minecraft.util.Identifier
import java.util.*

class ModelDatagen(generator: FabricDataGenerator) : FabricModelProvider(generator) {
    private companion object {
        val logger = Mod.logger<ModelDatagen>()
    }

    override fun generateBlockStateModels(generator: BlockStateModelGenerator) {
        logger.info("generating block state models")

        for ((id, deviceBlock) in Blocks.deviceBlocks) {
            val modelId = Identifier(Mod.id, "block/${id.path}")
            generator.modelCollector.accept(modelId) { DeviceBlockModel.generate(id, deviceBlock) }
            generator.blockStateCollector.accept(horizontallyRotatableBlockState(id, deviceBlock))
        }
    }

    private fun horizontallyRotatableBlockState(
        id: Identifier,
        deviceBlock: Blocks.RegisteredDeviceBlock,
    ): BlockStateSupplier {
        val modelId = Identifier(Mod.id, "block/${id.path}")
        return VariantsBlockStateSupplier.create(
            deviceBlock.block,
            BlockStateVariant.create().put(VariantSettings.MODEL, modelId)
        )
            .coordinate(BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates())
    }


    override fun generateItemModels(generator: ItemModelGenerator) {
        logger.info("generating item models")
        for ((id, deviceBlock) in Blocks.deviceBlocks) {
            generator.register(
                deviceBlock.item.item.item,
                Model(Optional.of(Identifier(Mod.id, "block/${id.path}")), Optional.empty())
            )
        }
    }
}