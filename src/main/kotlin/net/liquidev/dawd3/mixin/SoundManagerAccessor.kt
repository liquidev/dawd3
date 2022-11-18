package net.liquidev.dawd3.mixin

import net.minecraft.client.sound.SoundManager
import net.minecraft.client.sound.SoundSystem
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(SoundManager::class)
interface SoundManagerAccessor {
    @Accessor
    fun getSoundSystem(): SoundSystem
}
