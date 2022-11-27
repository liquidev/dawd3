package net.liquidev.dawd3.mixin

import net.liquidev.dawd3.render.Textures
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.util.SpriteIdentifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.function.Consumer

@Suppress("UNUSED")
@Mixin(TexturedRenderLayers::class)
abstract class NonModelTextures {
    private companion object {
        @Inject(
            method = ["addDefaultTextures(Ljava/util/function/Consumer;)V"],
            at = [At(value = "TAIL")]
        )
        @JvmStatic
        private fun addDefaultTextures(adder: Consumer<SpriteIdentifier>, ci: CallbackInfo) {
            for (texture in Textures.nonModel) {
                adder.accept(texture)
            }
        }
    }
}