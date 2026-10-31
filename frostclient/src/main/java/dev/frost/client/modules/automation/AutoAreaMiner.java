// AutoAreaMiner | Automation | Fabric 1.21.1
// grid mining: define a start pos, size X/Z, Y range
package dev.frost.client.modules.automation;

import dev.frost.client.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.*;

public class AutoAreaMiner extends Module {

    public BlockPos startPos  = BlockPos.ORIGIN;
    public int      sizeX     = 16;
    public int      sizeZ     = 16;
    public int      yFrom     = 60;
    public int      yTo       = 64;
    public int      breakDelay = 2;   // ticks between breaks
    public float    range     = 5.0f;

    private final Queue<BlockPos> targets = new LinkedList<>();
    private int delayTick = 0;

    public AutoAreaMiner() {
        super("AutoAreaMiner", Category.AUTOMATION);
        subscribeTick(this::tick);
    }

    @Override
    protected void onEnable() {
        targets.clear();
        // build grid in Y-layer order
        for (int y = yFrom; y <= yTo; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    targets.add(startPos.add(x, y - startPos.getY(), z));
                }
            }
        }
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;
        if (targets.isEmpty()) { forceDisable(); return; }
        if (delayTick++ < breakDelay) return;
        delayTick = 0;

        // grab next reachable target
        while (!targets.isEmpty()) {
            BlockPos pos = targets.peek();
            var state = mc.world.getBlockState(pos);
            if (state.isAir() || state.isOf(Blocks.WATER) || state.isOf(Blocks.LAVA)) {
                targets.poll(); continue;
            }
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > range * range) {
                // walk toward it — just break out, player must move manually or use baritone
                break;
            }
            // face and break
            float[] angs = dev.frost.client.util.CombatUtil.getAnglesTo(mc.player, Vec3d.ofCenter(pos));
            dev.frost.client.util.CombatUtil.setRotation(mc, angs[0], angs[1], false);
            mc.getNetworkHandler().sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP, 0));
            mc.getNetworkHandler().sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP, 0));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            targets.poll();
            break;
        }
    }
}
