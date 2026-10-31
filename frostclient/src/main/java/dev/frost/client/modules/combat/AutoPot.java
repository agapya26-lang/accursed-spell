// AutoPot | Combat | Fabric 1.21.1
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import dev.frost.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoPot extends Module {

    public float  threshold = 14.0f;   // health points (7 hearts)
    public int    delay     = 5;

    private int tick = 0;

    public AutoPot() {
        super("AutoPot", Category.COMBAT);
        subscribeTick(this::onTick);
    }

    private void onTick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null) return;
        if (tick++ < delay) return;
        tick = 0;

        float hp = mc.player.getHealth();
        if (hp > threshold) return;

        // find splash health potion in hotbar
        int slot = CombatUtil.findSplashHealthPot(mc);
        if (slot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        // look straight down so pot splashes on self
        float savedPitch = mc.player.getPitch();
        CombatUtil.setRotation(mc, mc.player.getYaw(), 90f, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), 90f));
        CombatUtil.setRotation(mc, mc.player.getYaw(), savedPitch, false);
        mc.player.getInventory().selectedSlot = prev;
    }
}
