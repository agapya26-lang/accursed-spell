// MaceAutomation | Combat | Fabric 1.21.1
// Auto Mace: swap to mace at peak fall, perfect timing for smash crit
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import dev.frost.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class MaceAutomation extends Module {

    // tune these
    public float  minFallDistance = 3.5f;   // blocks fallen before smash attempt
    public float  attackRange     = 4.0f;
    public boolean autoWindCharge = true;    // throw wind charge to boost yourself up first
    public int    windChargeDelay = 10;      // ticks after wind charge before mace swap

    private int   windDelay = 0;
    private boolean windChargeFired = false;

    public MaceAutomation() {
        super("MaceAutomation", Category.COMBAT);
        subscribeTick(this::tick);
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;

        var target = CombatUtil.nearestPlayer(mc, attackRange);
        if (target == null) { reset(); return; }

        // phase 1 — fire wind charge upward to gain height
        if (autoWindCharge && !windChargeFired && mc.player.isOnGround()) {
            int wSlot = CombatUtil.findInHotbar(mc, Items.WIND_CHARGE);
            if (wSlot != -1) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = wSlot;
                // look straight up then fire
                CombatUtil.setRotation(mc, mc.player.getYaw(), -89f, false);
                mc.options.useKey.setPressed(true);
                mc.options.useKey.setPressed(false);
                mc.player.getInventory().selectedSlot = prev;
                windChargeFired = true;
                windDelay = 0;
                return;
            }
        }

        if (windChargeFired && windDelay++ < windChargeDelay) return;

        // phase 2 — swap to mace when falling and target is below
        boolean isFalling      = mc.player.getVelocity().y < 0;
        float   fallDist       = mc.player.fallDistance;
        boolean targetBelow    = mc.player.getY() > target.getY();
        double  distToTarget   = mc.player.distanceTo(target);

        if (isFalling && fallDist >= minFallDistance && targetBelow && distToTarget <= attackRange) {
            int maceSlot = CombatUtil.findInHotbar(mc, Items.MACE);
            if (maceSlot == -1) { reset(); return; }

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = maceSlot;
            CombatUtil.faceEntity(mc, target, false);
            // attack — smash damage is calculated server-side from fallDistance
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            mc.player.getInventory().selectedSlot = prev;
            reset();
        }
    }

    private void reset() { windChargeFired = false; windDelay = 0; }
}
