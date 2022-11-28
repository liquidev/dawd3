package net.liquidev.dawd3.mixin;

import net.liquidev.dawd3.events.PlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerItemSwitchMixin {
    @Inject(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerEntity;selectedItem:Lnet/minecraft/item/ItemStack;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void fireItemSwitchedCallback(CallbackInfo callbackInfo) {
        PlayerEvents.ITEM_SWITCHED.invoker().itemSwitched((PlayerEntity) (Object) this);
    }
}
