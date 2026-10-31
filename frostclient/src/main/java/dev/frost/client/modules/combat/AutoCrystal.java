// AutoCrystal | Combat | Fabric 1.21.1
package dev.frost.client.modules.combat;

import dev.frost.client.FrostClient;
import dev.frost.client.modules.Module;
import dev.frost.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

public class AutoCrystal extends Module {

    // tunables — expose to GUI later
    public float  placeRange    = 5.0f;
    public float  breakRange    = 5.0f;
    public float  minDamage     = 4.0f;   // min dmg to target before placing
    public float  maxSelfDmg    = 10.0f;  // bail if self-damage exceeds this
    public int    placeDelay    = 1;       // ticks between placements
    public boolean swingArm    = true;

    private int placeTick = 0;
    private int breakTick = 0;

    public AutoCrystal() {
        super("AutoCrystal", Category.COMBAT);
        subscribeTick(this::tick);
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;

        PlayerEntity target = CombatUtil.nearestPlayer(mc, placeRange);
        if (target == null) return;

        // BREAK phase — attack existing crystals
        if (breakTick++ >= 1) {
            breakTick = 0;
            attackBestCrystal(mc, target);
        }

        // PLACE phase — find optimal position
        if (placeTick++ >= placeDelay) {
            placeTick = 0;
            placeOptimal(mc, target);
        }
    }

    private void placeOptimal(MinecraftClient mc, PlayerEntity target) {
        World world = mc.world;
        BlockPos targetPos = target.getBlockPos();

        // scan 3x1x3 around target feet
        BlockPos best = null;
        float   bestDmg = minDamage;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos candidate = targetPos.add(x, -1, z);
                if (!CombatUtil.isValidCrystalBase(world, candidate)) continue;
                if (mc.player.squaredDistanceTo(Vec3d.ofCenter(candidate)) > placeRange * placeRange) continue;

                float dmgToTarget = CombatUtil.crystalDamage(world, Vec3d.ofCenter(candidate.up()), target);
                float dmgToSelf   = CombatUtil.crystalDamage(world, Vec3d.ofCenter(candidate.up()), mc.player);

                if (dmgToSelf >= maxSelfDmg) continue;
                if (dmgToTarget > bestDmg) { bestDmg = dmgToTarget; best = candidate; }
            }
        }

        if (best == null) return;
        int prevSlot = mc.player.getInventory().selectedSlot;
        int crystalSlot = CombatUtil.findInHotbar(mc, Items.END_CRYSTAL);
        if (crystalSlot == -1) return;

        mc.player.getInventory().selectedSlot = crystalSlot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(best), Direction.UP, best, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        if (swingArm) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private void attackBestCrystal(MinecraftClient mc, PlayerEntity target) {
        EndCrystalEntity bestCrystal = null;
        float bestDmg = 0;

        for (EndCrystalEntity crystal : mc.world.getEntitiesByClass(
                EndCrystalEntity.class,
                mc.player.getBoundingBox().expand(breakRange), c -> true)) {

            float dmg = CombatUtil.crystalDamage(mc.world, crystal.getPos(), target);
            if (dmg > bestDmg) { bestDmg = dmg; bestCrystal = crystal; }
        }

        if (bestCrystal == null) return;
        // rotate silently toward crystal (silent aim style — no packet, just look)
        CombatUtil.faceEntity(mc, bestCrystal, true);
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(bestCrystal, mc.player.isSneaking()));
        if (swingArm) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }
}
