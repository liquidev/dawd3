package net.liquidev.dawd3.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.liquidev.dawd3.events.DebugHudEvents;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public class DebugHudMixin {
    @Inject(at = @At("RETURN"), method = "getLeftText")
    private void getLeftText(CallbackInfoReturnable<List<String>> info) {
        DebugHudEvents.ADD_DEBUG_INFO.invoker().addDebugInfo(info.getReturnValue());
    }
}
