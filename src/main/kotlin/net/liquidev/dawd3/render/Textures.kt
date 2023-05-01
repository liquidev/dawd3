package net.liquidev.dawd3.render

import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.liquidev.dawd3.Mod
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier

object Textures {

    /**
     * The set of textures that are not referenced by models but need to be loaded into the
     * block atlas.
     */
    private val nonModelBlockTextures = arrayOf(
        Identifier(Mod.id, "device/cable"),
    )

    fun initializeClient() {
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
            .register { _, registry ->
                for (id in nonModelBlockTextures) {
                    registry.register(id)
                }
            }
    }
}