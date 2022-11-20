package net.liquidev.dawd3.datagen

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.liquidev.dawd3.Mod

object Datagen : DataGeneratorEntrypoint {
    private val logger = Mod.logger<Datagen>()

    override fun onInitializeDataGenerator(datagen: FabricDataGenerator) {
        logger.info("initializing data generator")
        datagen.addProvider(::ModelDatagen)
    }
}