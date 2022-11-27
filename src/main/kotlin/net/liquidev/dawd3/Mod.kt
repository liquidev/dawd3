package net.liquidev.dawd3

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.liquidev.d3r.D3r
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.block.Blocks
import net.liquidev.dawd3.block.entity.registerClientBlockEntityEvents
import net.liquidev.dawd3.item.Items
import net.liquidev.dawd3.net.Packets
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNUSED")
object Mod : ModInitializer, ClientModInitializer {
    const val id = "dawd3"

    val logger = logger(null)

    override fun onInitialize() {
        logger.info("hello, sound traveler! welcome to the dawd³ experience")
        Blocks.initialize()
        Items.registry.registerAll()
    }

    override fun onInitializeClient() {
        logger.info("booting up sound engine")
        D3r.load()
        Audio.forceInitializationNow()

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            logger.info("shutting down sound engine")
            Audio.deinitialize()
            D3r.unload()
        }

        registerClientBlockEntityEvents()
        Blocks.initializeClient()
        Packets.registerClientReceivers()
    }

    private fun loggerName(name: String?): String =
        if (name != null) "$id/$name" else id

    fun logger(name: String?): Logger =
        LoggerFactory.getLogger(loggerName(name))

    inline fun <reified T> logger(): Logger =
        logger(T::class.simpleName)
}
