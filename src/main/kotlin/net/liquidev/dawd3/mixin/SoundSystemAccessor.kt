package net.liquidev.dawd3.mixin

import net.minecraft.client.sound.Channel
import net.minecraft.client.sound.SoundSystem
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(SoundSystem::class)
interface SoundSystemAccessor {
    @Accessor
    fun getChannel(): Channel
}