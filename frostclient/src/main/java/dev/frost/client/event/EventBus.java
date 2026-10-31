// EventBus — lightweight pub/sub for module hooks
package dev.frost.client.event;

import net.minecraft.client.MinecraftClient;
import java.util.*;
import java.util.function.Consumer;

public class EventBus {

    public enum Type { TICK, RENDER, PACKET_SEND, PACKET_RECV, ATTACK, MOVE }

    private final Map<Type, List<Consumer<FrostEvent>>> listeners = new EnumMap<>(Type.class);

    public void subscribe(Type type, Consumer<FrostEvent> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribe(Type type, Consumer<FrostEvent> listener) {
        var list = listeners.get(type);
        if (list != null) list.remove(listener);
    }

    public void fire(Type type, FrostEvent event) {
        var list = listeners.get(type);
        if (list != null) new ArrayList<>(list).forEach(c -> c.accept(event));
    }

    public void fireTick(MinecraftClient mc) {
        fire(Type.TICK, new FrostEvent.TickEvent(mc));
    }
}
