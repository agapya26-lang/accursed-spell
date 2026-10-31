// FastBreak | Automation | Fabric 1.21.1
// sends extra mining progress packets to saturate server mining speed
package dev.frost.client.modules.automation;

import dev.frost.client.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FastBreak extends Module {

    public int extraPackets = 3;   // extra progress packets per tick

    public static boolean enabled_flag = false;
    public static int     extra        = 3;

    public FastBreak() {
        super("FastBreak", Category.AUTOMATION);
        subscribeTick(this::tick);
    }

    @Override protected void onEnable()  { enabled_flag = true;  extra = extraPackets; }
    @Override protected void onDisable() { enabled_flag = false; }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (!(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult bhr)) return;

        BlockPos pos = bhr.getBlockPos();
        for (int i = 0; i < extra; i++) {
            mc.getNetworkHandler().sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP, 0));
        }
    }
}
