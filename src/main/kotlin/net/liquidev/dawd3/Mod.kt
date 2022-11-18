package net.liquidev.dawd3

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.liquidev.d3r.D3r
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.item.Items
import net.liquidev.dawd3.sound.Sound
import org.slf4j.LoggerFactory

@Suppress("UNUSED")
object Mod : ModInitializer, ClientModInitializer {
    const val id = "dawd3"

    private val logger = LoggerFactory.getLogger("dawd³")

    override fun onInitialize() {
        logger.info("hello, sound traveler! welcome to the dawd³ experience")

        Blocks.blockRegistry.registerAll()
        Items.registry.registerAll()
    }

    override fun onInitializeClient() {
        logger.info("booting up sound engine")
        D3r.load()
        Sound.forceInitializationNow()

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            logger.info("shutting down sound engine")
            Sound.deinitialize()
            D3r.unload()
        }
    }
}
