package net.liquidev.dawd3.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DebugHudEvents {
    public static Event<AddDebugInfo> ADD_DEBUG_INFO = EventFactory.createArrayBacked(
        AddDebugInfo.class,
        context -> {},
        callbacks -> paused -> {
            for (AddDebugInfo callback : callbacks) {
                callback.addDebugInfo(paused);
            }
        }
    );

    public interface AddDebugInfo {
        void addDebugInfo(List<String> debugInfo);
    }
}
