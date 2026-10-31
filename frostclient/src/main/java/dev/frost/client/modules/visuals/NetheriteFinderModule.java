// NetheriteFinderModule | Visuals | Fabric 1.21.1
// highlights ancient debris / chests in loaded chunks
package dev.frost.client.modules.visuals;

import dev.frost.client.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.*;

import java.util.*;

public class NetheriteFinderModule extends Module {

    public int    scanRadius = 64;
    public float  r = 1f, g = 0.7f, b = 0f;
    public static List<BlockPos> found = new ArrayList<>();
    public static boolean active = false;

    public NetheriteFinderModule() {
        super("NetheriteFinderModule", Category.VISUALS);
        subscribeTick(this::tick);
    }

    @Override protected void onEnable()  { active = true; found.clear(); }
    @Override protected void onDisable() { active = false; found.clear(); }

    private int scanTick = 0;

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;
        if (scanTick++ < 20) return;  // rescan every second
        scanTick = 0;

        BlockPos center = mc.player.getBlockPos();
        List<BlockPos> newFound = new ArrayList<>();
        for (int x = -scanRadius; x <= scanRadius; x += 1) {
            for (int y = 8; y <= 22; y++) {   // ancient debris Y range in nether
                for (int z = -scanRadius; z <= scanRadius; z += 1) {
                    BlockPos pos = center.add(x, y - center.getY(), z);
                    var state = mc.world.getBlockState(pos);
                    if (state.isOf(Blocks.ANCIENT_DEBRIS)
                        || state.isOf(Blocks.CHEST)
                        || state.isOf(Blocks.TRAPPED_CHEST)) {
                        newFound.add(pos.toImmutable());
                    }
                }
            }
        }
        found = newFound;
    }
}
