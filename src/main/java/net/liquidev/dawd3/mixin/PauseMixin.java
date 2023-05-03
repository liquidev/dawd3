package net.liquidev.dawd3.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.liquidev.dawd3.events.D3ClientEvents;
import net.minecraft.client.MinecraftClient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class PauseMixin {
    @Inject(
        method = "render(Z)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;paused:Z",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void onPause(boolean tick, CallbackInfo ci) {
        var client = (MinecraftClient) (Object) this;
        D3ClientEvents.PAUSE.invoker().pause(client.isPaused());
    }
}
