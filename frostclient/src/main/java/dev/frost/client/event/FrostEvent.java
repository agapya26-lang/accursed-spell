// FrostEvent — base + all event subtypes
package dev.frost.client.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public abstract class FrostEvent {
    public boolean cancelled = false;
    public void cancel() { cancelled = true; }

    public static class TickEvent extends FrostEvent {
        public final MinecraftClient mc;
        public TickEvent(MinecraftClient mc) { this.mc = mc; }
    }

    public static class RenderEvent extends FrostEvent {
        public final float delta;
        public RenderEvent(float delta) { this.delta = delta; }
    }

    public static class PacketSendEvent extends FrostEvent {
        public Packet<?> packet;
        public PacketSendEvent(Packet<?> packet) { this.packet = packet; }
    }

    public static class PacketRecvEvent extends FrostEvent {
        public Packet<?> packet;
        public PacketRecvEvent(Packet<?> packet) { this.packet = packet; }
    }

    public static class AttackEvent extends FrostEvent {
        public final net.minecraft.entity.Entity target;
        public AttackEvent(net.minecraft.entity.Entity target) { this.target = target; }
    }
}
