package net.liquidev.dawd3.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerEvents {
    public static Event<PlayerItemSwitch> ITEM_SWITCHED = EventFactory.createArrayBacked(
        PlayerItemSwitch.class,
        context -> {},
        callbacks -> context -> {
            for (PlayerItemSwitch callback : callbacks) {
                callback.itemSwitched(context);
            }
        }
    );

    public interface PlayerItemSwitch {
        void itemSwitched(PlayerEntity playerEntity);
    }
}
