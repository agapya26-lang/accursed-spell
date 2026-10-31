// SilentAim | Combat | Fabric 1.21.1
// rotates server-side only, camera stays where it is
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import dev.frost.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class SilentAim extends Module {

    public float range = 4.5f;

    // these are read by MixinGameRenderer to override outgoing rotation packets
    public static float serverYaw   = 0f;
    public static float serverPitch = 0f;
    public static boolean active    = false;

    public SilentAim() {
        super("SilentAim", Category.COMBAT);
        subscribeTick(this::tick);
    }

    @Override
    protected void onDisable() { active = false; }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) { active = false; return; }

        PlayerEntity target = CombatUtil.nearestPlayer(mc, range);
        if (target == null) { active = false; return; }

        Vec3d eye    = mc.player.getEyePos();
        Vec3d aimAt  = target.getPos().add(0, target.getEyeHeight(target.getPose()) * 0.8, 0);
        float[] angs = CombatUtil.getAnglesTo(mc.player, aimAt);

        serverYaw   = angs[0];
        serverPitch = angs[1];
        active      = true;
    }
}
