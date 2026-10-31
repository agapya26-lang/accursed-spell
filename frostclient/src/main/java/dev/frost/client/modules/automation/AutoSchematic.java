// AutoSchematic | Automation | Fabric 1.21.1
// Supports .litematic / .schem / .nbt — places up to 500K+ blocks
package dev.frost.client.modules.automation;

import dev.frost.client.modules.Module;
import dev.frost.client.util.SchematicUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.io.File;
import java.util.*;

public class AutoSchematic extends Module {

    public int    blocksPerTick = 4;
    public float  placeRange    = 5.0f;
    public boolean strictRotate = true;

    // loaded from file
    private List<SchematicUtil.PlaceEntry> queue = new ArrayList<>();
    private int queueIndex = 0;

    // set these before enabling
    public BlockPos origin  = BlockPos.ORIGIN;
    public File     schematicFile = null;

    public AutoSchematic() {
        super("AutoSchematic", Category.AUTOMATION);
        subscribeTick(this::tick);
    }

    @Override
    protected void onEnable() {
        if (schematicFile == null || !schematicFile.exists()) {
            FrostClient.LOG.warn("[AutoSchematic] no schematic loaded");
            forceDisable();
            return;
        }
        queue = SchematicUtil.load(schematicFile, origin);
        queueIndex = 0;
        FrostClient.LOG.info("[AutoSchematic] loaded {} blocks", queue.size());
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;
        if (queue.isEmpty() || queueIndex >= queue.size()) { forceDisable(); return; }

        int placed = 0;
        while (placed < blocksPerTick && queueIndex < queue.size()) {
            SchematicUtil.PlaceEntry entry = queue.get(queueIndex);
            queueIndex++;

            // skip if already correct
            if (mc.world.getBlockState(entry.pos).equals(entry.state)) continue;
            // skip if out of range
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(entry.pos)) > placeRange * placeRange) continue;

            int slot = SchematicUtil.findBlock(mc, entry.state);
            if (slot == -1) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            if (strictRotate) {
                float[] angs = dev.frost.client.util.CombatUtil.getAnglesTo(
                    mc.player, Vec3d.ofCenter(entry.pos));
                dev.frost.client.util.CombatUtil.setRotation(mc, angs[0], angs[1], false);
            }
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(entry.pos), Direction.UP, entry.pos, false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            mc.player.getInventory().selectedSlot = prev;
            placed++;
        }
    }
}
