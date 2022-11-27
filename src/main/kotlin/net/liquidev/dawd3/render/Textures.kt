package net.liquidev.dawd3.render

import net.liquidev.dawd3.Mod
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier

object Textures {
    /**
     * The set of textures that are not referenced by models but need to be loaded into the
     * block atlas.
     */
    val nonModel = setOf(
        SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            Identifier(Mod.id, "device/cable")
        )
    )
}