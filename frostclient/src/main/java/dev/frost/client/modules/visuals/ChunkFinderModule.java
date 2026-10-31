// ChunkFinderModule | Visuals | Fabric 1.21.1
// finds chunks with unusual chest/storage concentration (stash detection)
package dev.frost.client.modules.visuals;

import dev.frost.client.modules.Module;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.*;

import java.util.*;

public class ChunkFinderModule extends Module {

    public int   scanRadius   = 128;  // blocks around player
    public int   minChestCount = 3;   // min chests per chunk to flag
    public static Set<ChunkPos> flagged = new HashSet<>();
    public static boolean active = false;

    public ChunkFinderModule() {
        super("ChunkFinderModule", Category.VISUALS);
        subscribeTick(this::tick);
    }

    @Override protected void onEnable()  { active = true;  flagged.clear(); }
    @Override protected void onDisable() { active = false; flagged.clear(); }

    private int scanTick = 0;

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;
        if (scanTick++ < 40) return;
        scanTick = 0;

        BlockPos center = mc.player.getBlockPos();
        Map<ChunkPos, Integer> counts = new HashMap<>();

        for (int x = -scanRadius; x <= scanRadius; x += 1) {
            for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y++) {
                for (int z = -scanRadius; z <= scanRadius; z += 1) {
                    BlockPos pos = center.add(x, y - center.getY(), z);
                    Block b = mc.world.getBlockState(pos).getBlock();
                    if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST
                        || b == Blocks.BARREL || b == Blocks.SHULKER_BOX
                        || b == Blocks.ENDER_CHEST) {
                        ChunkPos cp = new ChunkPos(pos);
                        counts.merge(cp, 1, Integer::sum);
                    }
                }
            }
        }

        Set<ChunkPos> newFlagged = new HashSet<>();
        counts.forEach((cp, count) -> {
            if (count >= minChestCount) newFlagged.add(cp);
        });
        flagged = newFlagged;
    }
}
