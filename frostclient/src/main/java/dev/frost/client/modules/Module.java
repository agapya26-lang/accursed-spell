// Module — base for every feature
package dev.frost.client.modules;

import dev.frost.client.FrostClient;
import dev.frost.client.event.EventBus;
import dev.frost.client.event.FrostEvent;
import java.util.function.Consumer;

public abstract class Module {

    public enum Category { COMBAT, AUTOMATION, VISUALS, SECURITY }

    public final String   name;
    public final Category category;
    protected boolean     enabled = false;

    public Module(String name, Category category) {
        this.name     = name;
        this.category = category;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void forceDisable() {
        if (enabled) { enabled = false; onDisable(); }
    }

    public boolean isEnabled() { return enabled; }

    // subclasses override these
    protected void onEnable()  {}
    protected void onDisable() {}

    // helper — subscribe to tick while enabled
    protected void subscribeTick(Consumer<FrostEvent.TickEvent> handler) {
        FrostClient.eventBus.subscribe(EventBus.Type.TICK, e -> {
            if (enabled && e instanceof FrostEvent.TickEvent te) handler.accept(te);
        });
    }

    protected void subscribeRender(Consumer<FrostEvent.RenderEvent> handler) {
        FrostClient.eventBus.subscribe(EventBus.Type.RENDER, e -> {
            if (enabled && e instanceof FrostEvent.RenderEvent re) handler.accept(re);
        });
    }
}
