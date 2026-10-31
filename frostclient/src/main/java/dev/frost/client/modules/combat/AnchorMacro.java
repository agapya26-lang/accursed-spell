// AnchorMacro | Combat | Fabric 1.21.1
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import dev.frost.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

public class AnchorMacro extends Module {

    public float  placeRange  = 4.5f;
    public float  minDamage   = 6.0f;
    public float  maxSelfDmg  = 8.0f;
    public int    chargeDelay = 2;   // ticks to wait between charge clicks

    private int   state = 0;   // 0=place, 1=charge, 2=detonate
    private BlockPos anchorPos = null;
    private int   delayTick = 0;

    public AnchorMacro() {
        super("AnchorMacro", Category.COMBAT);
        subscribeTick(this::tick);
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;

        var target = CombatUtil.nearestPlayer(mc, placeRange + 2);
        if (target == null) { reset(); return; }

        switch (state) {
            case 0 -> placeAnchor(mc, target);
            case 1 -> chargeAnchor(mc);
            case 2 -> detonateAnchor(mc, target);
        }
    }

    private void placeAnchor(MinecraftClient mc, net.minecraft.entity.player.PlayerEntity target) {
        int slot = CombatUtil.findInHotbar(mc, Items.RESPAWN_ANCHOR);
        if (slot == -1) return;

        BlockPos targetPos = target.getBlockPos();
        // place 1 block beside target at foot level
        for (Direction dir : Direction.values()) {
            BlockPos candidate = targetPos.offset(dir);
            if (!mc.world.getBlockState(candidate).isAir()) continue;
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(candidate)) > placeRange * placeRange) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(candidate), dir.getOpposite(), candidate, false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            mc.player.getInventory().selectedSlot = prev;
            anchorPos = candidate;
            state = 1;
            delayTick = 0;
            return;
        }
    }

    private void chargeAnchor(MinecraftClient mc) {
        if (anchorPos == null) { reset(); return; }
        if (delayTick++ < chargeDelay) return;
        delayTick = 0;

        int slot = CombatUtil.findInHotbar(mc, Items.GLOWSTONE);
        if (slot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anchorPos), Direction.UP, anchorPos, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.getInventory().selectedSlot = prev;

        // assume fully charged after 4 charges (max)
        if (++chargeCount >= 4) { state = 2; chargeCount = 0; }
    }
    private int chargeCount = 0;

    private void detonateAnchor(MinecraftClient mc, net.minecraft.entity.player.PlayerEntity target) {
        if (anchorPos == null) { reset(); return; }
        // safety check — make sure we're not in the overworld / end
        if (mc.world.getDimension().natural()) { reset(); return; }  // natural = overworld

        float selfDmg = CombatUtil.explosionDamage(mc.world, Vec3d.ofCenter(anchorPos), mc.player, 5f);
        if (selfDmg >= maxSelfDmg) { reset(); return; }

        int slot = CombatUtil.findInHotbar(mc, Items.RESPAWN_ANCHOR);
        if (slot == -1) { reset(); return; }

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anchorPos), Direction.UP, anchorPos, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.getInventory().selectedSlot = prev;
        reset();
    }

    private void reset() { state = 0; anchorPos = null; chargeCount = 0; delayTick = 0; }
}
