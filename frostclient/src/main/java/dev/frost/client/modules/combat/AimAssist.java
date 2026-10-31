// AimAssist | Combat | Fabric 1.21.1
// prediction + smoothing toward nearest player
package dev.frost.client.modules.combat;

import dev.frost.client.modules.Module;
import dev.frost.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {

    public float  range         = 5.5f;
    public float  speed         = 5.0f;    // degrees per tick max
    public float  fov           = 90.0f;   // only assist inside this fov cone
    public boolean prediction   = true;    // lead target by velocity
    public float  predictionMs  = 100f;    // how far ahead to predict (ms)

    public AimAssist() {
        super("AimAssist", Category.COMBAT);
        subscribeTick(this::tick);
    }

    private void tick(dev.frost.client.event.FrostEvent.TickEvent e) {
        MinecraftClient mc = e.mc;
        if (mc.player == null || mc.world == null) return;

        PlayerEntity target = CombatUtil.nearestPlayerInFov(mc, range, fov);
        if (target == null) return;

        Vec3d aimPos = target.getPos().add(0, target.getEyeHeight(target.getPose()) * 0.8, 0);

        // predict by velocity
        if (prediction) {
            Vec3d vel = target.getVelocity();
            float ticks = predictionMs / 50f;
            aimPos = aimPos.add(vel.multiply(ticks));
        }

        float[] angles = CombatUtil.getAnglesTo(mc.player, aimPos);
        float   dyaw   = CombatUtil.deltaAngle(mc.player.getYaw(),   angles[0]);
        float   dpitch = CombatUtil.deltaAngle(mc.player.getPitch(), angles[1]);

        float   step   = Math.min(speed, 1f);   // lerp fraction per tick
        float   newYaw   = mc.player.getYaw()   + dyaw   * step;
        float   newPitch = mc.player.getPitch() + dpitch * step;

        // clamp pitch
        newPitch = Math.max(-90f, Math.min(90f, newPitch));

        CombatUtil.setRotation(mc, newYaw, newPitch, false);
    }
}
