// TriggerBot | Combat | Fabric 1.21.1
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class TriggerBot extends Module {

    public int  delay       = 2;   // ticks between attacks
    public boolean teamCheck = true;

    private int tick = 0;

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        subscribeTick(this::onTick);
    }

    private void onTick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (tick++ < delay) return;
        tick = 0;

        // check if crosshair is on a player
        if (mc.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerEntity target) {
                mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }
}
