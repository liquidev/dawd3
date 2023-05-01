package net.liquidev.dawd3.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public class D3ClientEvents {
    public static Event<Pause> PAUSE = EventFactory.createArrayBacked(
        Pause.class,
        context -> {},
        callbacks -> paused -> {
            for (Pause callback : callbacks) {
                callback.pause(paused);
            }
        }
    );

    public interface Pause {
        void pause(boolean paused);
    }
}
